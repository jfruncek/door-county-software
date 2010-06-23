/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package play

def name='jfruncek'

println "Hello $name!"

println "${new Date().format('dd MMMM yyyy')}"

GroovyTestSuite.main(["test/play/JUnit3StyleTest.groovy"].toArray(new String()))
