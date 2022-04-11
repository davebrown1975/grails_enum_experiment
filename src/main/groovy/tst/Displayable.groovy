package tst

import grails.core.GrailsApplication
import org.springframework.beans.factory.annotation.Autowired

trait Displayable {

  String displayName() {
   def what = this
    println what
  }
}