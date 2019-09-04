package dsp.cache

import com.google.common.cache.{CacheBuilder, CacheLoader, LoadingCache}
import com.twitter.cache.guava.GuavaCache
import com.twitter.util.Future
import java.time.Instant
import java.util.concurrent.TimeUnit

trait GCache { 
  val getMapCache: String => Future[Map[String, Double]]
}

object GCache extends GCache {

  private[this] val cache: LoadingCache[String, Future[Map[String, Double]]] = {
    CacheBuilder.newBuilder
      .maximumSize(5)
      .expireAfterWrite(1, TimeUnit.MINUTES)
      .build(new CacheLoader[String, Future[Map[String, Double]]]() {
        def load(key: String): Future[Map[String, Double]] =
          getMap(key)
      })
  }

  val getMapCache: String => Future[Map[String, Double]] = {
    key => GuavaCache.fromLoadingCache(cache)(key)
  }

  private[this] def getMap(key: String): Future[Map[String, Double]] = {
    Thread.sleep(5000)
    Future(Map("A" -> 1.2, "B" -> 1.4))
  }
}
