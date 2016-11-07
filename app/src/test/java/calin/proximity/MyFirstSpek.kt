package calin.proximity

import io.kotlintest.specs.BehaviorSpec

class MyTests : BehaviorSpec() {
    init {
        Given("a broomstick") {
            When("I sit on it") {
                Then("I should be able to fly") {
                    // test code
                }
            }
            When("I throw it away") {
                Then("it should come back") {
                    // test code
                }
            }
        }
    }
}
