package tst

import grails.compiler.ast.AstTransformer
import grails.util.Holders
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.runtime.StringGroovyMethods
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.grails.compiler.injection.GrailsASTUtils
import org.springframework.context.i18n.LocaleContextHolder

import static groovyjarjarasm.asm.Opcodes.ACC_PUBLIC

@Slf4j
@AstTransformer
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
@CompileStatic
class EnumTranslationTransformation implements ASTTransformation {

//  private static final Logger LOG = LoggerFactory.getLogger( EnumTranslationTransformation.class )
  private CompilationUnit compilationUnit;

  @Override
  public void visit(ASTNode[] nodes, SourceUnit source) {
    println "In Visit"
    // this.sourceUnit = sourceUnit
    println StringGroovyMethods.plus(StringGroovyMethods.plus(StringGroovyMethods.plus(ExampleEnum.getCanonicalName(), "."), "name"), ".label")
    ExpandoMetaClass.disableGlobally()

    final List<ClassNode> classNodes = source?.getAST()?.classes ?: []
    classNodes.each { ClassNode classNode ->
      try {
        if (classNode.superClass.name == Enum.class.canonicalName) {
          classNode.addMethod(createDisplayNameMethod())
          classNode.addMethod(createDisplayNameMethodWithLocale())
          GrailsASTUtils.processVariableScopes(source, classNode)
          println("displayName() and displayName(Locale) methods added to Enum '${classNode.name}'")
        }
      }
      catch (Exception e) {
        GrailsASTUtils.error(source, classNode, "Error during Enum AST Transformation: ${e.message}")
        throw e
      }
    }

    ExpandoMetaClass.enableGlobally()
  }

  private MethodNode createDisplayNameMethod() {
    new AstBuilder().buildFromSpec {
      method('displayName', ACC_PUBLIC, String) {
        parameters {
        }
        exceptions {}
        block {
          expression {
            declaration {
              variable 'messageSource'
              token '='
              methodCall {
                property {
                  methodCall {
                    classExpression Holders
                    constant 'getGrailsApplication'
                    argumentList {}
                  }
                  constant 'mainContext'
                }
                constant 'getBean'
                argumentList {
                  constant 'messageSource'
                }
              }
            }
          }
          expression {
            declaration {
              variable 'name'
              token '='
              methodCall {
                variable 'this'
                constant 'name'
                argumentList {}
              }
            }
          }
          returnStatement {
            methodCall {
              variable 'messageSource'
              constant 'getMessage'
              argumentList {
                binary {
                  binary {
                    binary {
                      property {
                        property {
                          variable 'this'
                          constant 'class'
                        }
                        constant 'canonicalName'
                      }
                      token '+'
                      constant '.'
                    }
                    token '+'
                    variable 'name'
                  }
                  token '+'
                  constant '.label'
                }
                constant null
                variable 'name'
                methodCall {
                  classExpression LocaleContextHolder
                  constant 'getLocale'
                  argumentList {}
                }
              }
            }
          }
        }
        annotations {}
      }
    }.first() as MethodNode
  }

  private MethodNode createDisplayNameMethodWithLocale() {
    new AstBuilder().buildFromSpec {
      method('displayName', ACC_PUBLIC, String) {
        parameters {
          parameter 'locale': Locale.class
        }
        exceptions {}
        block {
          expression {
            declaration {
              variable 'messageSource'
              token '='
              methodCall {
                property {
                  methodCall {
                    classExpression Holders
                    constant 'getGrailsApplication'
                    argumentList {}
                  }
                  constant 'mainContext'
                }
                constant 'getBean'
                argumentList {
                  constant 'messageSource'
                }
              }
            }
          }
          expression {
            declaration {
              variable 'name'
              token '='
              methodCall {
                variable 'this'
                constant 'name'
                argumentList {}
              }
            }
          }
          returnStatement {
            methodCall {
              variable 'messageSource'
              constant 'getMessage'
              argumentList {
                binary {
                  binary {
                    binary {
                      property {
                        property {
                          variable 'this'
                          constant 'class'
                        }
                        constant 'canonicalName'
                      }
                      token '+'
                      constant '.'
                    }
                    token '+'
                    variable 'name'
                  }
                  token '+'
                  constant '.label'
                }
                constant null
                variable 'name'
                elvisOperator {
                  variable 'locale'
                  methodCall {
                    classExpression LocaleContextHolder
                    constant 'getLocale'
                    argumentList {}
                  }
                }
              }
            }
          }
        }
        annotations {}
      }
    }.first() as MethodNode
  }
}
