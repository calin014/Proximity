package calin.proximity.core

import calin.proximity.core.abstractions.drivers.*
import rx.Observer
import rx.subjects.PublishSubject

/**
 * Created by calin on 11/4/2016.
 */

object GameRunner {
    fun run(game: ProximityGame,
            locationDriver: LocationDriver,
            userInterfaceDriver: UserInterfaceDriver,
            mapDriver: MapDriver,
            repositoryDriver: RepositoryDriver) {

        val sources: GameSources = GameSources(
                LocationSources(PublishSubject.create()),
                RepositorySources(PublishSubject.create(), PublishSubject.create()),
                UserInterfaceSources(PublishSubject.create(), PublishSubject.create(), PublishSubject.create()),
                MapSources(PublishSubject.create(), PublishSubject.create())
        )

        val (repositorySinks, userInterfaceSinks, mapSinks) = game.main(sources)

        val (sLocation) = locationDriver.main(Unit)
        val (sCenterButtonClicks, sPlaceBombButtonClicks, sDefuseBombButtonClicks) = userInterfaceDriver.main(userInterfaceSinks)
        val (sBombClicks, sOutsideClicks) = mapDriver.main(mapSinks)
        val (sPlayer, sBombEvent) = repositoryDriver.main(repositorySinks)

        sLocation.subscribe(ProxyObserver(sources.locationSources.sLocation as PublishSubject))
        sCenterButtonClicks.subscribe(ProxyObserver(sources.userInterfaceSources.sCenterButtonClicks as PublishSubject))
        sPlaceBombButtonClicks.subscribe(ProxyObserver(sources.userInterfaceSources.sPlaceBombButtonClicks as PublishSubject))
        sDefuseBombButtonClicks.subscribe(ProxyObserver(sources.userInterfaceSources.sDefuseBombButtonClicks as PublishSubject))
        sBombClicks.subscribe(ProxyObserver(sources.mapSources.sBombClicks as PublishSubject))
        sOutsideClicks.subscribe(ProxyObserver(sources.mapSources.sOutsideClicks as PublishSubject))
        sPlayer.subscribe(ProxyObserver(sources.repositorySources.sPlayer as PublishSubject))
        sBombEvent.subscribe(ProxyObserver(sources.repositorySources.sBombEvent as PublishSubject))
    }

    class ProxyObserver<T>(val proxy: PublishSubject<T>) : Observer<T> {
        override fun onError(e: Throwable) {
            proxy.onError(e)
        }

        override fun onCompleted() {
            proxy.onCompleted()
        }

        override fun onNext(t: T) {
            proxy.onNext(t)
        }
    }
}