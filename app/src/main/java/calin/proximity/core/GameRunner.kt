package calin.proximity.core

import calin.proximity.core.abstractions.drivers.LocationDriver
import calin.proximity.core.abstractions.drivers.MapDriver
import calin.proximity.core.abstractions.drivers.RepositoryDriver
import calin.proximity.core.abstractions.drivers.UserInterfaceDriver

/**
 * Created by calin on 11/4/2016.
 */

object GameRunner {
    fun run(game: ProximityGame,
            locationDriver: LocationDriver,
            userInterfaceDriver: UserInterfaceDriver,
            mapDriver: MapDriver,
            repositoryDriver: RepositoryDriver) {

        val sources:GameSources = createGameSourcesProxy()
        val (repositorySinks, userInterfaceSinks, mapSinks) = game.main(sources)

        val locationSources = locationDriver.main(Unit)
        val (sCenterButtonClicks, sPlaceBombButtonClicks, sDefuseBombButtonClicks) = userInterfaceDriver.main(userInterfaceSinks)
        val (sBombClicks, sOutsideClicks) = mapDriver.main(mapSinks)
        val (sPlayer, sBombEvent) = repositoryDriver.main(repositorySinks)

    }

    private fun createGameSourcesProxy(): GameSources {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}