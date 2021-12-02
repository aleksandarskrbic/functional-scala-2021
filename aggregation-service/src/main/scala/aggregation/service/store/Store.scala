package aggregation.service.store

import zio._

sealed abstract class Store[Key, Event, Aggregate] {
  def find(key: Key): UIO[Option[Aggregate]]
  def append(key: Key, event: Event): UIO[Unit]
}

object Store {
  def make[Key, Event, Aggregate](
      initFn: Event => Aggregate,
      aggregateFn: (Event, Aggregate) => Aggregate
  ): UIO[Store[Key, Event, Aggregate]] =
    Ref.make(Map.empty[Key, Aggregate]).map { ref =>
      new Store[Key, Event, Aggregate] {
        override def find(key: Key): UIO[Option[Aggregate]] =
          ref.get.map(_.get(key))

        override def append(key: Key, event: Event): UIO[Unit] =
          ref.get.flatMap { kvs =>
            kvs.get(key) match {
              case Some(aggregate) =>
                ref.update(_ + (key -> aggregateFn(event, aggregate)))
              case None => ref.update(_ + (key -> initFn(event)))
            }
          }
      }
    }
}
