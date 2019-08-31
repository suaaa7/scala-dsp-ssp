package cache

import com.github.benmanes.caffeine.cache.{Caffeine, LoadingCache}
import com.twitter.cache.caffeine.CaffeineCache
import com.twitter.util.Future
import java.time.Instant
import java.util.concurrent.TimeUnit

trait CCache { 
  val getMapCache: String => Future[Map[String, Double]]
}

object CCache extends CCache {

  private[this] val cache: LoadingCache[String, Future[Map[String, Double]]] = {
    Caffeine.newBuilder
      .asInstanceOf[Caffeine[String, Future[Map[String, Double]]]]
      .maximumSize(5)
      .expireAfterWrite(10, TimeUnit.MINUTES)
      .refreshAfterWrite(1, TimeUnit.MINUTES)
      .build(key => getMap(key))
  }

  val getMapCache: String => Future[Map[String, Double]] = {
    key => CaffeineCache.fromLoadingCache(cache)(key)
  }

  private[this] def getMap(key: String): Future[Map[String, Double]] = {
    Thread.sleep(5000)
    Future(Map("A" -> 0.03, "B" -> 0.04))
  }
}
