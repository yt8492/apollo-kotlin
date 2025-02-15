package com.apollographql.ijplugin.inspection

import com.apollographql.ijplugin.ApolloBundle
import com.apollographql.ijplugin.project.apolloProjectService
import com.apollographql.ijplugin.util.getMethodName
import com.apollographql.ijplugin.util.lambdaBlockExpression
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.findParentInFile
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtVisitorVoid

class ApolloEndpointNotConfiguredInspection : LocalInspectionTool() {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return object : KtVisitorVoid() {
      override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (!expression.project.apolloProjectService.apolloVersion.isAtLeastV3) return
        if (expression.containingFile.name != "build.gradle.kts") return
        if (expression.getMethodName() == "service" && expression.findParentInFile { it is KtCallExpression && it.getMethodName() == "apollo" } != null) {
          val serviceBlockExpression = expression.lambdaBlockExpression() ?: return
          if (serviceBlockExpression.statements.none { it is KtCallExpression && it.getMethodName() == "introspection" }) {
            holder.registerProblem(expression.calleeExpression!!, ApolloBundle.message("inspection.endpointNotConfigured.reportText"), AddIntrospectionBlockQuickFix)
          }
        }
      }
    }
  }
}

object AddIntrospectionBlockQuickFix : LocalQuickFix {
  override fun getName() = ApolloBundle.message("inspection.endpointNotConfigured.quickFix")
  override fun getFamilyName() = name

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val callExpression = descriptor.psiElement.parent as KtCallExpression
    val serviceBlockExpression = callExpression.lambdaBlockExpression() ?: return
    val ktFactory = KtPsiFactory(project)
    val newCallExpression = ktFactory.createExpression(
        """
        introspection {
            endpointUrl.set("https://example.com/graphql")
            headers.put("api-key", "1234567890abcdef")
            schemaFile.set(file("src/main/graphql/schema.graphqls"))
        }
      """.trimIndent()
    )
    serviceBlockExpression.add(ktFactory.createNewLine())
    serviceBlockExpression.add(newCallExpression)
  }
}
