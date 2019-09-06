package dsp.cache

import com.twitter.util.{Await, Future}

object Cache {
  def warmUp(): Unit = {
    val f = Future
      .join(
        GCache.getMapCache("Guava"),
        CCache.getMapCache("Caffeine"),
      )
      .unit

    Await.result(f)
  }
}
