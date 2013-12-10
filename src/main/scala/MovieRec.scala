package moviedemo

import org.jblas.DoubleMatrix
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD
import util.Random
import scala.util._
import java.io._
import scala.io.Source

object MovieRec {

  def main(args: Array[String]){


    val options =  args.map { arg =>
      arg.dropWhile(_ == '-').split('=') match {
        case Array(opt, v) => (opt -> v)
        case Array(opt) => (opt -> "true")
        case _ => throw new IllegalArgumentException("Invalid argument: " + arg)
      }
    }.toMap

    // read in input, defaults
    val master = options.getOrElse("master", "local[4]")
    val userfeats = options.getOrElse("userfeats", "")
    val moviefeats = options.getOrElse("moviefeats", "")
    val jar = options.getOrElse("jar", "")
    val sparkhome = options.getOrElse("sparkhome", System.getenv("SPARK_HOME"))
    val userid = options.getOrElse("userid", "").toInt
    val titles = options.getOrElse("titles", "")

    // print out input
    println("master:       " + master);     println("userfeats:    " + userfeats)
    println("moviefeats:   " + moviefeats); println("jar:          " + jar)
    println("sparkhome:    " + sparkhome);  println("userid:       " + userid)
    println("titles:       " + titles)

    // start spark context
    val sc = new SparkContext(master, "MovieRec",sparkhome,List(jar));

    val user = sc.textFile(userfeats)
    val uservec = new DoubleMatrix( user.map{ line => line.split(",") }
        .filter{ a => a(0).toInt == userid }
        .map{ a => a(1).split(" ").map(b => b.toDouble) }
        .collect() )

    val movie = sc.textFile(moviefeats)
    val moviemat = new DoubleMatrix( movie.map{ line => line.split(",") }
        .map{ a => a(1).split(" ").map(b => b.toDouble) }
        .collect() )

    val recs = moviemat.mmul(uservec)
    val order = recs.sortingPermutation().slice(0,20)

    val top = sc.textFile(titles)
        .map{ line => line.split("::") }
        .filter{ a => order.contains(a(0)) }
        .collect()

    top.map{ x => println(x(2)) }

    sc.stop()

  }

}