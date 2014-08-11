package test.model

import org.scalatest.Matchers
import org.scalatest.prop.PropertyChecks
import org.scalatest.FlatSpec
import com.github.nscala_time.time.Imports._
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.Gen.Choose
import slogger.model.common.TimePeriod
import slogger.model.specification.extraction.SlicingSpecs


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
  
  "First complete slice start date" should "be greater or equal to interval start date" in {
    forAll(Gen.choose(origin.minusDays(10), origin.plusDays(10))) { (snapToDate: DateTime) =>
      val slices = SlicingSpecs(sliceDuration, snapToDate).getSlicesFor(interval).filter(_.complete)
      slices(0).start should be >= interval.start
    }
  }
    
  it should "be less then sliceDuration near interval start date" in {
    forAll(Gen.choose(origin.minusDays(10), origin.plusDays(10))) { (snapToDate: DateTime) =>
      val slices = SlicingSpecs(sliceDuration, snapToDate).getSlicesFor(interval).filter(_.complete)      
      Math.abs(slices(0).start.getMillis() - interval.start.getMillis()) should be <= sliceDuration.getMillis()
    }    
  }
  
  "Slice start date" should "be N*sliceDuration from snapTo date for all slices except first" in {
    forAll(Gen.choose(origin.minusDays(10), origin.plusDays(10))) { (snapToDate: DateTime) =>
      val slices = SlicingSpecs(sliceDuration, snapToDate).getSlicesFor(interval)
      slices.drop(1).foreach { slice => 
        Math.abs(slice.start.getMillis() - snapToDate.getMillis()) % sliceDuration.getMillis() shouldBe 0 
      }
    }
  }
  
  "Last complete slice end date" should "be less or equal to interval end date" in {
    forAll(Gen.choose(origin.minusDays(10), origin.plusDays(10))) { (snapToDate: DateTime) =>
      val slices = SlicingSpecs(sliceDuration, snapToDate).getSlicesFor(interval).filter(_.complete)
      slices.last.end should be <= interval.end
    }
  }
  
  it should "be less then sliceDuration near interval end date" in {
    forAll(Gen.choose(origin.minusDays(10), origin.plusDays(10))) { (snapToDate: DateTime) =>
      val slices = SlicingSpecs(sliceDuration, snapToDate).getSlicesFor(interval).filter(_.complete)      
      Math.abs(slices.last.end.getMillis() - interval.end.getMillis()) should be <= sliceDuration.getMillis()
    }    
  }
  
  "Slice end date" should "be N*sliceDuration from snapTo date  for all slices except last" in {
    forAll(Gen.choose(origin.minusDays(10), origin.plusDays(10))) { (snapToDate: DateTime) =>
      val slices = SlicingSpecs(sliceDuration, snapToDate).getSlicesFor(interval)
      slices.take(slices.length - 1).foreach { slice => 
        Math.abs(slice.end.getMillis() - snapToDate.getMillis()) % sliceDuration.getMillis() shouldBe 0 
      }
    }
  }
 
  
  "slices count" should "be minimal enough to cover all interval" in {
    forAll(Gen.choose(origin.minusDays(10), origin.plusDays(10))) { (snapToDate: DateTime) =>
      val slices = SlicingSpecs(sliceDuration, snapToDate).getSlicesFor(interval)      
      Seq(requiredSliceCount, requiredSliceCount +1) should contain (slices.length)      
    }
  } 
   
  val requiredSliceCount = Math.ceil(interval.toDurationMillis().toDouble / sliceDuration.getMillis()).toInt
  
    
  def isNested(outer: Interval, inner: Interval): Boolean = outer.contains(inner.start) && outer.contains(inner.end)
}