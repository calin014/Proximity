package calin.proximity.core

import calin.proximity.App
import calin.proximity.core.abstractions.DistanceCalculator
import calin.proximity.core.abstractions.drivers.*
import calin.proximity.core.abstractions.drivers.BombEventType.ADDED
import calin.proximity.core.abstractions.drivers.BombEventType.REMOVED
import rx.Observable
import java.util.*
import java.util.concurrent.TimeUnit


data class GameSources(
        val locationSources: LocationSources,
        val repositorySources: RepositorySources,
        val userInterfaceSources: UserInterfaceSources,
        val mapSources: MapSources
)

data class GameSinks(
        val repositorySinks: RepositorySinks,
        val userInterfaceSinks: UserInterfaceSinks,
        val mapSinks: MapSinks
)

//TODO: MindYourStepsGamePlay, BombHuntGamePlay
class ProximityGame(val distanceCalculator: DistanceCalculator) : App<GameSources, GameSinks> {

    private val DETONATION_RADIUS = 5
    private val DEFUSING_RADIUS = 10
    private val TIME_TO_BECOME_ACTIVE = 1000 * 60

    override fun main(sources: GameSources): GameSinks {
        val sBombsOnMap = bombsOnMap(sources)
        val sBombInArea = bombInPlayerArea(sBombsOnMap, sources)

        return GameSinks(
                mapSinks = mapSinks(sources),
                repositorySinks = repositorySinks(sources),
                userInterfaceSinks = userInterfaceSinks(sBombInArea, sources)
        )
    }

    private fun bombInPlayerArea(sBombsOnMap: Observable<HashSet<ProximityBomb>>?, sources: GameSources): Observable<BombDistance> {
        return Observable.timer(1, TimeUnit.SECONDS)
                .withLatestFrom(sources.locationSources.sLocation, sBombsOnMap, { tick, location, bombsOnMap ->
                    nearestBomb(location,
                            bombsOnMap
                                    .filter { System.currentTimeMillis() - it.timestamp >= TIME_TO_BECOME_ACTIVE })
                })
                .filter { it.bomb != null && it.distance <= DEFUSING_RADIUS }
    }

    private fun bombsOnMap(sources: GameSources): Observable<HashSet<ProximityBomb>>? {
        return sources.repositorySources.sBombEvent.scan(HashSet<ProximityBomb>()) { set, bombEvent ->
            when (bombEvent.type) {
                ADDED -> set.add(bombEvent.proximityBomb)
                REMOVED -> set.remove(bombEvent.proximityBomb)
            }
            set
        }
    }

    private fun userInterfaceSinks(sBombInArea: Observable<BombDistance>, sources: GameSources): UserInterfaceSinks {
        return UserInterfaceSinks(
                sDefuseButtonVisibility = Observable.merge(
                        sources.mapSources.sBombClicks.map { true },
                        sources.mapSources.sOutsideClicks.map { false }
                ).startWith(false),
                sBombDefusingArea = sBombInArea.filter { it.distance > DETONATION_RADIUS }.map { it.bomb },
                sBombExploded = sBombInArea.filter { it.distance <= DETONATION_RADIUS }.map { it.bomb }
        )
    }

    private fun repositorySinks(sources: GameSources): RepositorySinks {
        return RepositorySinks(
                sBombAdded = sources.userInterfaceSources.sPlaceBombButtonClicks
                        .withLatestFrom(sources.locationSources.sLocation, sources.repositorySources.sPlayer,
                                { click, location, player ->
                                    ProximityBomb(location = location,
                                            timestamp = System.currentTimeMillis(/*this should be added on server*/),
                                            placer = player)
                                }),
                sBombRemoved = sources.userInterfaceSources.sDefuseBombButtonClicks
                        .withLatestFrom(sources.mapSources.sBombClicks, { click, bomb -> bomb }),
                sInterestArea = sources.locationSources.sLocation.first().map { InterestArea(it, 100.0) }
        )
    }

    private fun mapSinks(sources: GameSources): MapSinks {
        return MapSinks(
                sBombRemoved = sources.repositorySources.sBombEvent.filter { it.type == REMOVED }.map { it.proximityBomb },
                sBombAdded = sources.repositorySources.sBombEvent.filter { it.type == ADDED }.map { it.proximityBomb },
                sCenter = sources.userInterfaceSources.sCenterButtonClicks.startWith(Unit) //center at the beginning
                        .withLatestFrom(sources.locationSources.sLocation, { click, location -> location })
        )
    }


    class BombDistance(var bomb: ProximityBomb?, var distance: Double)

    private fun nearestBomb(location: Location, bombs: List<ProximityBomb>) =
            bombs.fold(BombDistance(null, Double.MAX_VALUE), {
                result, bomb ->
                val distance = distanceCalculator.calculate(location, bomb.location)
                if (result.distance > distance) {
                    result.bomb = bomb
                    result.distance = distance
                }
                result
            })
}