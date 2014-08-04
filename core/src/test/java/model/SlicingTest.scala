package model

import org.scalatest.Matchers
import org.scalatest.prop.PropertyChecks
import org.scalatest.FlatSpec
import com.github.nscala_time.time.Imports._
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.Gen.Choose
import slogger.model.TimePeriod
import slogger.model.specification.extraction.Slicing


class SlicingTest extends FlatSpec with Matchers with PropertyChecks {
  
  implicit val abc: Arbitrary[DateTime] = Arbitrary(Gen.choose(0L, Long.MaxValue).map(new DateTime(_)))
  
  implicit val chooseDateTime: Choose[DateTime] = new Choose[DateTime] {
    def choose(low: DateTime, high: DateTime) =
      if (low > high) fail
      else Gen.choose(low.getMillis(), high.getMillis()).map(new DateTime(_))
  }
  
  val origin = new DateTime("2013-08-01T10:00:0.0Z")
  
  val interval = new Interval(origin.minusDays(5), origin.plusDays(5))
  val sliceDuration = new Duration(84483654L)//about 1 day
  
  "Slice sequence start date" should "be less or equal to interval start date" in {
    forAll(Gen.choose(origin.minusDays(10), origin.plusDays(10))) { (snapToDate: DateTime) =>
      val slices = Slicing(snapToDate, sliceDuration).getSlicesFor(interval)
      slices(0).start should be <= interval.start
    }
  }
    
  it should "be less then sliceDuration near interval start date" in {
    forAll(Gen.choose(origin.minusDays(10), origin.plusDays(10))) { (snapToDate: DateTime) =>
      val slices = Slicing(snapToDate, sliceDuration).getSlicesFor(interval)      
      Math.abs(slices(0).start.getMillis() - interval.start.getMillis()) should be <= sliceDuration.getMillis()
    }    
  }
  
  it should "be N*sliceDuration from snapTo date" in {
    forAll(Gen.choose(origin.minusDays(10), origin.plusDays(10))) { (snapToDate: DateTime) =>
      val slices = Slicing(snapToDate, sliceDuration).getSlicesFor(interval)
      Math.abs(slices(0).start.getMillis() - snapToDate.getMillis()) % sliceDuration.getMillis() shouldBe 0
    }
  }
  
  "Slice sequence end date" should "be greater or equal to interval end date" in {
    forAll(Gen.choose(origin.minusDays(10), origin.plusDays(10))) { (snapToDate: DateTime) =>
      val slices = Slicing(snapToDate, sliceDuration).getSlicesFor(interval)
      slices.last.end should be >= interval.end
    }
  }
  
  it should "be less then sliceDuration near interval end date" in {
    forAll(Gen.choose(origin.minusDays(10), origin.plusDays(10))) { (snapToDate: DateTime) =>
      val slices = Slicing(snapToDate, sliceDuration).getSlicesFor(interval)      
      Math.abs(slices.last.end.getMillis() - interval.end.getMillis()) should be <= sliceDuration.getMillis()
    }    
  }
  
  it should "be N*sliceDuration from snapTo date" in {
    forAll(Gen.choose(origin.minusDays(10), origin.plusDays(10))) { (snapToDate: DateTime) =>
      val slices = Slicing(snapToDate, sliceDuration).getSlicesFor(interval)
      Math.abs(slices.last.end.getMillis() - snapToDate.getMillis()) % sliceDuration.getMillis() shouldBe 0
    }
  }
 
  
  "slices count" should "be minimal enough to cover all interval" in {
    forAll(Gen.choose(origin.minusDays(10), origin.plusDays(10))) { (snapToDate: DateTime) =>
      val slices = Slicing(snapToDate, sliceDuration).getSlicesFor(interval)      
      Seq(requiredSliceCount, requiredSliceCount +1) should contain (slices.length)      
    }
  } 
   
  val requiredSliceCount = Math.ceil(interval.toDurationMillis().toDouble / sliceDuration.getMillis()).toInt
}