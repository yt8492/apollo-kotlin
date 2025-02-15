package com.apollographql.apollo3.ast

import com.apollographql.apollo3.ast.internal.ExecutableValidationScope
import kotlin.jvm.JvmOverloads

fun GQLOperationDefinition.rootTypeDefinition(schema: Schema) = when (operationType) {
  "query" -> schema.queryTypeDefinition
  "mutation" -> schema.mutationTypeDefinition
  "subscription" -> schema.subscriptionTypeDefinition
  else -> null
}

@JvmOverloads
fun GQLOperationDefinition.validate(
    schema: Schema,
    fragments: Map<String, GQLFragmentDefinition>,
    fieldsOnDisjointTypesMustMerge: Boolean = true,
) = ExecutableValidationScope(schema, fragments, fieldsOnDisjointTypesMustMerge).validateOperation(this)