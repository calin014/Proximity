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

class ProximityGame(val distanceCalculator: DistanceCalculator) : App<GameSources, GameSinks> {

    private val DETONATION_RADIUS = 5
    private val DEFUSING_RADIUS = 10
    private val TIME_TO_BECOME_ACTIVE = 1000 * 60

    override fun main(sources: GameSources): GameSinks {
        val sBombsOnMap = sBombsOnMap(sources.repositorySources.sBombEvent)
        val sBombInArea = sBombInPlayerArea(sBombsOnMap, sources.locationSources.sLocation)

        return GameSinks(
                RepositorySinks(
                        sBombAdded(
                                sources.userInterfaceSources.sPlaceBombButtonClicks,
                                sources.locationSources.sLocation,
                                sources.repositorySources.sPlayer),
                        sBombRemoved(
                                sources.userInterfaceSources.sDefuseBombButtonClicks,
                                sources.mapSources.sBombClicks),
                        sInterestArea(sources.locationSources.sLocation)
                ),
                UserInterfaceSinks(
                        sDefuseButtonVisibility(
                                sources.mapSources.sBombClicks.map { Unit },
                                sources.mapSources.sOutsideClicks),
                        sBombDefusingArea(sBombInArea),
                        sBombExploded(sBombInArea)
                ),
                MapSinks(
                        sBombRemoved(sources.repositorySources.sBombEvent),
                        sBombAdded(sources.repositorySources.sBombEvent),
                        sCenter(
                                sources.locationSources.sLocation,
                                sources.userInterfaceSources.sCenterButtonClicks)
                )
        )
    }

    fun sCenter(sLocation: Observable<Location>, sCenterButtonClicks: Observable<Unit>): Observable<Location> {
        return sLocation.first().concatWith(//center map in the beginning
                sCenterButtonClicks //then center on button clicks
                        .withLatestFrom(sLocation, { click, location -> location }))
    }

    fun sBombAdded(sBombEvent: Observable<BombEvent>): Observable<ProximityBomb> =
            sBombEvent.filter { it.type == ADDED }.map { it.proximityBomb }

    fun sBombRemoved(sBombEvent: Observable<BombEvent>): Observable<ProximityBomb> =
            sBombEvent.filter { it.type == REMOVED }.map { it.proximityBomb }

    fun sInterestArea(sLocation: Observable<Location>): Observable<InterestArea> =
            sLocation.first().map { InterestArea(it, 100.0) }

    fun sBombRemoved(sDefuseBombButtonClicks: Observable<Unit>, sBombClicks: Observable<ProximityBomb>): Observable<ProximityBomb> =
            sDefuseBombButtonClicks.withLatestFrom(sBombClicks, { click, bomb -> bomb })

    fun sBombAdded(
            sPlaceBombButtonClicks: Observable<Unit>,
            sLocation: Observable<Location>,
            sPlayer: Observable<Player>): Observable<ProximityBomb> =
            sPlaceBombButtonClicks
                    .withLatestFrom(sLocation, sPlayer,
                            { click, location, player ->
                                ProximityBomb(location = location,
                                        timestamp = System.currentTimeMillis(/*this should be added on server*/),
                                        placer = player)
                            })

    fun sBombInPlayerArea(sBombsOnMap: Observable<HashSet<ProximityBomb>>, sLocation: Observable<Location>): Observable<BombDistance> =
            Observable.timer(1, TimeUnit.SECONDS)
                    .withLatestFrom(sLocation, sBombsOnMap, { tick, location, bombsOnMap ->
                        nearestBomb(location,
                                bombsOnMap
                                        .filter { System.currentTimeMillis() - it.timestamp >= TIME_TO_BECOME_ACTIVE })
                    })
                    .filter { it.bomb != null && it.distance <= DEFUSING_RADIUS }

    fun sBombsOnMap(sBombEvent: Observable<BombEvent>): Observable<HashSet<ProximityBomb>> =
            sBombEvent.scan(HashSet<ProximityBomb>()) { set, bombEvent ->
                when (bombEvent.type) {
                    ADDED -> set.add(bombEvent.proximityBomb)
                    REMOVED -> set.remove(bombEvent.proximityBomb)
                }
                set
            }

    fun sBombExploded(sBombInArea: Observable<BombDistance>): Observable<ProximityBomb> =
            sBombInArea.filter { it.distance <= DETONATION_RADIUS }.map { it.bomb }

    fun sBombDefusingArea(sBombInArea: Observable<BombDistance>): Observable<ProximityBomb> =
            sBombInArea.filter { it.distance > DETONATION_RADIUS }.map { it.bomb }

    fun sDefuseButtonVisibility(sBombClicks: Observable<Unit>, sOutsideClicks: Observable<Unit>): Observable<Boolean> =
            Observable.merge(sBombClicks.map { true }, sOutsideClicks.map { false }).startWith(false)

    class BombDistance(var bomb: ProximityBomb?, var distance: Double)

    fun nearestBomb(location: Location, bombs: List<ProximityBomb>) =
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