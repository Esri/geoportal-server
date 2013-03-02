/* See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Esri Inc. licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.framework.sql;
import com.esri.gpt.framework.util.Val;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Aids in the process of binding variables to SQL expressions.
 */
public class ExpressionBinder {
    
  /** class variables ========================================================= */
  
  /** Logical AND operator. */
  private static final LogicalOperator LOGICAL_AND = new LogicalOperator("AND");
  
  /** Logical OR operator.*/
  private static final LogicalOperator LOGICAL_OR = new LogicalOperator("OR");
   
  /** instance variables ====================================================== */
  private Expressions expressions = new Expressions(ExpressionBinder.LOGICAL_AND);
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public ExpressionBinder() {}
  
  /** methods ================================================================= */
  
  /**
   * Adds a bound field filter to the expression.
   * @param field the field name
   * @param operator the operator examples: =, !=, >, >=, <, <=, LIKE, ...
   * @param binding the variable to bind
   */
  public void addBinding(String field, String operator, Object binding) {
    Object[] bindings = {binding};
    this.addBindings(field,operator,bindings);
  }

  /**
   * Adds a bound field filter to the expression.
   * <br/>This method is useful for VARCHAR type fields.
   * @param field the field name
   * @param binding the variable to bind
   * @param forceUpper force an upper case comparison
   * @param forceLike for a LIKE operator 
   */
  public void addBinding(String field, String binding, boolean forceUpper, boolean forceLike) {
    String[] bindings = {binding};
    this.addBindings(field,bindings,forceUpper,forceLike);
  }
  
  /**
   * Adds a collection of bound field filters to the expression.
   * <br/>The collection is connected by logical "OR" operators.
   * @param field the field name
   * @param operator the operator examples: =, !=, >, >=, <, <=, LIKE, ...
   * @param bindings the array of variables to bind
   */
  public void addBindings(String field, String operator, Object[] bindings) {
    if (bindings.length == 1) {
      this.expressions.addPart(makePart(field,operator,bindings[0]));
    } else {
      Expressions subclause = new Expressions(ExpressionBinder.LOGICAL_OR);
      for (Object binding: bindings) {
        subclause.addPart(makePart(field,operator,binding));
      }
      this.expressions.merge(subclause);
    }
  }
  
  /**
   * Adds a collection of bound field filters to the expression.
   * <br/>The collection is connected by a logical "OR" operators.
   * <br/>This method is useful for VARCHAR type fields.
   * @param field the field name
   * @param bindings the array of variables to bind
   * @param forceUpper force an upper case comparison
   * @param forceLike for a LIKE operator 
   */
  public void addBindings(String field, String[] bindings, boolean forceUpper, boolean forceLike) {
    if (bindings.length == 1) {
      this.expressions.addPart(makePart(field,bindings[0],forceUpper,forceLike));
    } else {
      Expressions subclause = new Expressions(ExpressionBinder.LOGICAL_OR);
      for (String binding: bindings) {
        subclause.addPart(makePart(field,binding,forceUpper,forceLike));
      }
      this.expressions.merge(subclause);
    }
  }
  
  /**
   * Adds a sub-clause expression.
   * <br/>If the sub-clause expression is unbound, pass null for the bindings argument.
   * @param expression the sub-clause expression
   * @param bindings the array of variables bound to the clause
   */
  public void addClause(String expression, Object[] bindings) {
    this.expressions.addClause(expression,bindings);
  }
  
  /**
   * Binds variables to a JDBC prepared statement.
   * @param statement the prepared statement
   * @param startIndex the starting index for bound statement variables
   * @return the next index to use for bound statement variables
   * @throws SQLException if an exception occurs while binding
   */
  public int applyBindings(PreparedStatement statement, int startIndex) throws SQLException {
    for (Object binding: this.getBindings()) {
      statement.setObject(startIndex++,binding);
    }  
    return startIndex;
  }
  
  /**
   * Gets a list of objects bound to the expression.
   * @return the bound objects
   */
  public List<Object> getBindings() {
    return this.expressions.getBindings();
  }
  
  /**
   * Gets the SQL where clause expression.
   * @param includeWhereKeyword if true, prefix with "WHERE" if the expression is not empty
   * @return the SQL where clause
   */
  public String getExpression(boolean includeWhereKeyword) {
    String expression = Val.chkStr(this.expressions.asExpression());
    if (includeWhereKeyword && (expression.length() > 0)) {
      expression = " WHERE "+expression;
    }
    return expression;
  }
  
  /**
   * Makes an expression part.
   * @param field the field name
   * @param operator the operator examples: =, !=, >, >=, <, <=, LIKE, ...
   * @param binding the variable to bind
   * @return the expression part
   */
  private ExpressionPart makePart(String field, String operator, Object binding) {
    return new ExpressionPart(field+" "+operator+" ?",binding);
  }
  
  /**
   * Makes an expression part.
   * <br/>This method is useful for VARCHAR type fields.
   * @param field the field name
   * @param binding the variable to bind
   * @param forceUpper force an upper case comparison
   * @param forceLike for a LIKE operator 
   * @return the expression part
   */
  private ExpressionPart makePart(String field, String binding, boolean forceUpper, boolean forceLike) {
    if (binding == null) {
      return new ExpressionPart(field+" = ?",binding);
    } else {
      String expression = "";
      binding = binding.replaceAll("\\*","%");
      if (forceUpper) {
        field = "UPPER("+field+")";
        binding = binding.toUpperCase();
      }
      if (binding.indexOf("%") != -1) {
        expression = field+" LIKE ?";
      } else if (forceLike) {
        binding = "%"+binding+"%";
        expression = field+" LIKE ?";
      } else {
        expression = field+" = ?";
      }
      return new ExpressionPart(expression,binding);
    }
  }
  
  /** inner classes =========================================================== */
  
  /**
   * Defines a part consisting of an expression and a bound variable.
   */
  private static class ExpressionPart {
    private Object binding;
    private String expression;
    
    /**
     * Constructor.
     * @param expression the expression
     * @param binding the bound variable
     */
    private ExpressionPart(String expression, Object binding) {
      
      // validate
      expression = Val.chkStr(expression);
      int nPlaceholders = Expressions.countPlaceholders(expression);
      if (expression.length() == 0) {
        throw new IllegalArgumentException("The expression was empty.");
      } else if (nPlaceholders != 1) {
        String msg = "The expression must contain one ?";
        throw new IllegalArgumentException(msg);
      }
      
      this.expression = expression;
      this.binding = binding;
    }
    
    /**
     * Gets the expression.
     * @return the expression
     */
    private String getExpression() {
      return this.expression;
    }
    
    /**
     * Gets the binding.
     * @return the bound variable
     */
    private Object getBinding() {
      return this.binding;
    }
  }

  
  private static class Expressions {
    private List<Object> bindings = new ArrayList<Object>();
    private List<String> expressions = new ArrayList<String>();
    private LogicalOperator logicalOperator;   
    
    /**
     * Constructor.
     * @param logicalOperator the logical operator for the collection
     */
    private Expressions(LogicalOperator logicalOperator) {
      this.logicalOperator = logicalOperator;
    }
    
    /**
     * Gets a list of objects bound to the expression collection.
     * @return the bound objects
     */
    private List<Object> getBindings() {
      return this.bindings;
    }
        
    /**
     * Adds a sub-clause expression to the collection.
     * <br/>If the sub-clause expression is unbound, pass null for the bindings argument.
     * @param expression the sub-clause expression
     * @param bindings the array of variables bound to the clause
     */
    private void addClause(String expression, Object[] bindings) {
      
      // validate
      expression = Val.chkStr(expression);
      int nPlaceholders = Expressions.countPlaceholders(expression);
      int nBindings = (bindings == null) ? 0: bindings.length;
      if (expression.length() == 0) {
        throw new IllegalArgumentException("The expression was empty.");
      } else if (nPlaceholders != nBindings) {
        String msg = "Binding mismatch: ? count = "+nPlaceholders+
                     ", binding count = "+nBindings;
        throw new IllegalArgumentException(msg);
      } 
      
      this.expressions.add(expression);
      if (bindings != null) {
        for (Object binding: bindings) {
          this.bindings.add(binding);
        }
      }
    }
    
    /**
     * Adds a part to the collection.
     * <br/>The part will not be added if it is null.
     * @param part the part to add
     */
    private void addPart(ExpressionPart part) {
      if (part != null) {
        this.expressions.add(part.getExpression());
        this.bindings.add(part.getBinding());
      }
    }
    
    /**
     * Makes a SQL expression from the collection.
     * @return the SQL expression
     */
    private String asExpression() {
      StringBuffer clause = new StringBuffer();
      String connector = " "+this.logicalOperator.getConnector()+" ";
      for (String expression: this.expressions) {
        if (clause.length() > 0) clause.append(connector);
        clause.append(expression);
      }
      if (clause.length() > 0) clause.insert(0,"(").append(")");
      return clause.toString();
    }
    
    /**
     * Counts the number of binding placeholders (question marks) in an expression.
     * @param expression the expression 
     * @return the number of binding placeholders
     */
    private static int countPlaceholders(String expression) {
      int nPlaceholders = 0;
      if (expression != null) {
        for (int i=0;i<expression.length();i++) {
          if (expression.charAt(i) == '?') {
            nPlaceholders++;
          }
        }
      }
      return nPlaceholders;
    }
    
    /**
     * Merges a collection od sub-expressions into this collection.
     * @param subexpressions the expressions to merge
     */
    private void merge(Expressions subExpressions) {
      if (subExpressions != null) {
        String expression = Val.chkStr(subExpressions.asExpression());
        if (expression.length() > 0) {
          this.expressions.add(expression);
          if (subExpressions.getBindings() != null) {
            for (Object binding: subExpressions.getBindings()) {
              this.bindings.add(binding);
            }
          }
        }
      }
    }
    
  }
  
  /**
   * Defines a logical operator.
   */
  private static class LogicalOperator {
    String connector;
    
    /**
     * Constructor.
     * @param connector the SQL connector string
     */
    private LogicalOperator(String connector) {
      this.connector = connector;
    }
    
    /**
     * Gets the SQL connector string.
     * @return the SQL connector string
     */
    private String getConnector() {
      return this.connector;
    }
  }
  
}
