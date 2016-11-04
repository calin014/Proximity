package calin.proximity

/**
 * Created by calin on 11/4/2016.
 */

interface Driver<I, O> {
    fun main(sinks: I): O
}

interface App<I, O> {
    fun main(sources: I): O
}
//
//interface Cycle<APP, DRIVER> {
//    fun run(app: APP, vararg drivers: Driver<*, *>)
//}