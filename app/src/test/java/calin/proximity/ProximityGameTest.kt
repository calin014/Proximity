package calin.proximity

import calin.proximity.android.AndroidDistanceCalculator
import calin.proximity.core.ProximityBomb
import calin.proximity.core.ProximityGame
import calin.proximity.core.abstractions.drivers.BombEvent
import calin.proximity.core.abstractions.drivers.BombEventType.ADDED
import calin.proximity.core.abstractions.drivers.BombEventType.REMOVED
import io.kotlintest.specs.BehaviorSpec
import rx.Observable
import rx.observers.TestSubscriber

class ProximityGameTest : BehaviorSpec() {
    init {
        val game = ProximityGame(AndroidDistanceCalculator)

        Given("a bomb event stream with two events") {
            val addedBomb = ProximityBomb()
            val removedBomb = ProximityBomb()
            val sBombEvent = Observable.just(BombEvent(ADDED, addedBomb), BombEvent(REMOVED, removedBomb))

            When("the bomb added stream is created and a subscription takes place") {
                val sBombAdded = game.sBombAdded(sBombEvent)
                val testSubscriber = TestSubscriber.create<ProximityBomb>()
                sBombAdded.subscribe(testSubscriber)

                Then("the subscriber should receive the added bomb") {
                    testSubscriber.assertNoErrors()
                    testSubscriber.assertValues(addedBomb)
                }
            }

            When("the bomb removed stream is created and a subscription takes place") {
                val sBombRemoved = game.sBombRemoved(sBombEvent)
                val testSubscriber = TestSubscriber.create<ProximityBomb>()
                sBombRemoved.subscribe(testSubscriber)

                Then("the subscriber should receive the removed bomb") {
                    testSubscriber.assertNoErrors()
                    testSubscriber.assertValues(removedBomb)
                }
            }
        }
    }
}
