/* Generated By:JJTree: Do not edit this line. OSelectStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
/*


 */
package com.orientechnologies.orient.core.sql.parser;

import com.orientechnologies.orient.core.command.OBasicCommandContext;
import com.orientechnologies.orient.core.command.OCommandContext;
import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.sql.OCommandSQLParsingException;
import com.orientechnologies.orient.core.sql.executor.*;
import com.orientechnologies.orient.core.storage.OStorage;

import java.util.HashMap;
import java.util.Map;

public class OSelectStatement extends OStatement {

  protected OFromClause target;

  protected OProjection projection;

  protected OWhereClause whereClause;

  protected OGroupBy groupBy;

  protected OOrderBy orderBy;

  protected OUnwind unwind;

  protected OSkip skip;

  protected OLimit limit;

  protected OStorage.LOCKING_STRATEGY lockRecord = null;

  protected OFetchPlan fetchPlan;

  protected OLetClause letClause;

  protected OTimeout timeout;

  protected Boolean parallel;

  protected Boolean noCache;

  public OSelectStatement(int id) {
    super(id);
  }

  public OSelectStatement(OrientSql p, int id) {
    super(p, id);
  }

  public OProjection getProjection() {
    return projection;
  }

  public void setProjection(OProjection projection) {
    this.projection = projection;
  }

  public OFromClause getTarget() {
    return target;
  }

  public void setTarget(OFromClause target) {
    this.target = target;
  }

  public OWhereClause getWhereClause() {
    return whereClause;
  }

  public void setWhereClause(OWhereClause whereClause) {
    this.whereClause = whereClause;
  }

  public OGroupBy getGroupBy() {
    return groupBy;
  }

  public void setGroupBy(OGroupBy groupBy) {
    this.groupBy = groupBy;
  }

  public OOrderBy getOrderBy() {
    return orderBy;
  }

  public void setOrderBy(OOrderBy orderBy) {
    this.orderBy = orderBy;
  }

  public OSkip getSkip() {
    return skip;
  }

  public void setSkip(OSkip skip) {
    this.skip = skip;
  }

  public OLimit getLimit() {
    return limit;
  }

  public void setLimit(OLimit limit) {
    this.limit = limit;
  }

  public OStorage.LOCKING_STRATEGY getLockRecord() {
    return lockRecord;
  }

  public void setLockRecord(OStorage.LOCKING_STRATEGY lockRecord) {
    this.lockRecord = lockRecord;
  }

  public OFetchPlan getFetchPlan() {
    return fetchPlan;
  }

  public void setFetchPlan(OFetchPlan fetchPlan) {
    this.fetchPlan = fetchPlan;
  }

  public OLetClause getLetClause() {
    return letClause;
  }

  public void setLetClause(OLetClause letClause) {
    this.letClause = letClause;
  }

  public void toString(Map<Object, Object> params, StringBuilder builder) {

    builder.append("SELECT");
    if (projection != null) {
      builder.append(" ");
      projection.toString(params, builder);
    }
    if (target != null) {
      builder.append(" FROM ");
      target.toString(params, builder);
    }

    if (letClause != null) {
      builder.append(" ");
      letClause.toString(params, builder);
    }

    if (whereClause != null) {
      builder.append(" WHERE ");
      whereClause.toString(params, builder);
    }

    if (groupBy != null) {
      builder.append(" ");
      groupBy.toString(params, builder);
    }

    if (orderBy != null) {
      builder.append(" ");
      orderBy.toString(params, builder);
    }

    if (unwind != null) {
      builder.append(" ");
      unwind.toString(params, builder);
    }

    if (skip != null) {
      skip.toString(params, builder);
    }

    if (limit != null) {
      limit.toString(params, builder);
    }

    if (lockRecord != null) {
      builder.append(" LOCK ");
      switch (lockRecord) {
      case DEFAULT:
        builder.append("DEFAULT");
        break;
      case EXCLUSIVE_LOCK:
        builder.append("RECORD");
        break;
      case SHARED_LOCK:
        builder.append("SHARED");
        break;
      case NONE:
        builder.append("NONE");
        break;
      }
    }

    if (fetchPlan != null) {
      builder.append(" ");
      fetchPlan.toString(params, builder);
    }

    if (timeout != null) {
      timeout.toString(params, builder);
    }

    if (Boolean.TRUE.equals(parallel)) {
      builder.append(" PARALLEL");
    }

    if (Boolean.TRUE.equals(noCache)) {
      builder.append(" NOCACHE");
    }
  }

  public void validate() throws OCommandSQLParsingException {
    if (projection != null) {
      projection.validate();
      if (projection.isExpand() && groupBy != null) {
        throw new OCommandSQLParsingException("expand() cannot be used together with GROUP BY");
      }
    }
  }

  @Override
  public boolean executinPlanCanBeCached() {
    if (originalStatement == null) {
      setOriginalStatement(this.toString());
    }
    if (this.target != null && !this.target.isCacheable()) {
      return false;
    }

    if (this.letClause != null && !this.letClause.isCacheable()) {
      return false;
    }

    if (projection != null && !this.projection.isCacheable()) {
      return false;
    }

    if (whereClause != null && !whereClause.isCacheable()) {
      return false;
    }

    return true;
  }

  @Override
  public OResultSet execute(ODatabase db, Object[] args, OCommandContext parentCtx) {
    OBasicCommandContext ctx = new OBasicCommandContext();
    if (parentCtx != null) {
      ctx.setParentWithoutOverridingChild(parentCtx);
    }
    ctx.setDatabase(db);
    Map<Object, Object> params = new HashMap<>();
    if (args != null) {
      for (int i = 0; i < args.length; i++) {
        params.put(i, args[i]);
      }
    }
    ctx.setInputParameters(params);
    OInternalExecutionPlan executionPlan = createExecutionPlan(ctx, false);

    OLocalResultSet result = new OLocalResultSet(executionPlan);
    return result;
  }

  @Override
  public OResultSet execute(ODatabase db, Map params, OCommandContext parentCtx) {
    OBasicCommandContext ctx = new OBasicCommandContext();
    if (parentCtx != null) {
      ctx.setParentWithoutOverridingChild(parentCtx);
    }
    ctx.setDatabase(db);
    ctx.setInputParameters(params);
    OInternalExecutionPlan executionPlan = createExecutionPlan(ctx, false);

    OLocalResultSet result = new OLocalResultSet(executionPlan);
    return result;
  }

  public OInternalExecutionPlan createExecutionPlan(OCommandContext ctx, boolean enableProfiling) {
    OSelectExecutionPlanner planner = new OSelectExecutionPlanner(this);
    OInternalExecutionPlan result = planner.createExecutionPlan(ctx, enableProfiling, true);
    result.setStatement(this.originalStatement);
    return result;
  }

  public OInternalExecutionPlan createExecutionPlanNoCache(OCommandContext ctx, boolean enableProfiling) {
    OSelectExecutionPlanner planner = new OSelectExecutionPlanner(this);
    OInternalExecutionPlan result = planner.createExecutionPlan(ctx, enableProfiling, false);
    result.setStatement(this.originalStatement);
    return result;
  }

  @Override
  public OSelectStatement copy() {
    OSelectStatement result = null;
    try {
      result = getClass().getConstructor(Integer.TYPE).newInstance(-1);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    result.originalStatement = originalStatement;
    result.target = target == null ? null : target.copy();
    result.projection = projection == null ? null : projection.copy();
    result.whereClause = whereClause == null ? null : whereClause.copy();
    result.groupBy = groupBy == null ? null : groupBy.copy();
    result.orderBy = orderBy == null ? null : orderBy.copy();
    result.unwind = unwind == null ? null : unwind.copy();
    result.skip = skip == null ? null : skip.copy();
    result.limit = limit == null ? null : limit.copy();
    result.lockRecord = lockRecord;
    result.fetchPlan = fetchPlan == null ? null : fetchPlan.copy();
    result.letClause = letClause == null ? null : letClause.copy();
    result.timeout = timeout == null ? null : timeout.copy();
    result.parallel = parallel;
    result.noCache = noCache;

    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    OSelectStatement that = (OSelectStatement) o;

    if (target != null ? !target.equals(that.target) : that.target != null)
      return false;
    if (projection != null ? !projection.equals(that.projection) : that.projection != null)
      return false;
    if (whereClause != null ? !whereClause.equals(that.whereClause) : that.whereClause != null)
      return false;
    if (groupBy != null ? !groupBy.equals(that.groupBy) : that.groupBy != null)
      return false;
    if (orderBy != null ? !orderBy.equals(that.orderBy) : that.orderBy != null)
      return false;
    if (unwind != null ? !unwind.equals(that.unwind) : that.unwind != null)
      return false;
    if (skip != null ? !skip.equals(that.skip) : that.skip != null)
      return false;
    if (limit != null ? !limit.equals(that.limit) : that.limit != null)
      return false;
    if (lockRecord != that.lockRecord)
      return false;
    if (fetchPlan != null ? !fetchPlan.equals(that.fetchPlan) : that.fetchPlan != null)
      return false;
    if (letClause != null ? !letClause.equals(that.letClause) : that.letClause != null)
      return false;
    if (timeout != null ? !timeout.equals(that.timeout) : that.timeout != null)
      return false;
    if (parallel != null ? !parallel.equals(that.parallel) : that.parallel != null)
      return false;
    if (noCache != null ? !noCache.equals(that.noCache) : that.noCache != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = target != null ? target.hashCode() : 0;
    result = 31 * result + (projection != null ? projection.hashCode() : 0);
    result = 31 * result + (whereClause != null ? whereClause.hashCode() : 0);
    result = 31 * result + (groupBy != null ? groupBy.hashCode() : 0);
    result = 31 * result + (orderBy != null ? orderBy.hashCode() : 0);
    result = 31 * result + (unwind != null ? unwind.hashCode() : 0);
    result = 31 * result + (skip != null ? skip.hashCode() : 0);
    result = 31 * result + (limit != null ? limit.hashCode() : 0);
    result = 31 * result + (lockRecord != null ? lockRecord.hashCode() : 0);
    result = 31 * result + (fetchPlan != null ? fetchPlan.hashCode() : 0);
    result = 31 * result + (letClause != null ? letClause.hashCode() : 0);
    result = 31 * result + (timeout != null ? timeout.hashCode() : 0);
    result = 31 * result + (parallel != null ? parallel.hashCode() : 0);
    result = 31 * result + (noCache != null ? noCache.hashCode() : 0);
    return result;
  }

  @Override
  public boolean refersToParent() {
    //no FROM, if a subquery refers to parent it does not make sense, so that reference will be just ignored

    if (projection != null && projection.refersToParent()) {
      return true;
    }
    if (whereClause != null && whereClause.refersToParent()) {
      return true;
    }
    if (groupBy != null && groupBy.refersToParent()) {
      return true;
    }
    if (orderBy != null && orderBy.refersToParent()) {
      return true;
    }
    if (letClause != null && letClause.refersToParent()) {
      return true;
    }
    return false;
  }

  public OUnwind getUnwind() {
    return unwind;
  }

  @Override
  public boolean isIdempotent() {
    return true;
  }

  public void setUnwind(OUnwind unwind) {
    this.unwind = unwind;
  }

  public void setTimeout(OTimeout timeout) {
    this.timeout = timeout;
  }

  public void setParallel(Boolean parallel) {
    this.parallel = parallel;
  }

  public void setNoCache(Boolean noCache) {
    this.noCache = noCache;
  }

  public OResult serialize() {
    OResultInternal result = (OResultInternal) super.serialize();
    if (target != null) {
      result.setProperty("target", target.serialize());
    }
    if (projection != null) {
      result.setProperty("projection", projection.serialize());
    }
    if (whereClause != null) {
      result.setProperty("whereClause", whereClause.serialize());
    }
    if (groupBy != null) {
      result.setProperty("groupBy", groupBy.serialize());
    }
    if (orderBy != null) {
      result.setProperty("orderBy", orderBy.serialize());
    }
    if (unwind != null) {
      result.setProperty("unwind", unwind.serialize());
    }
    if (skip != null) {
      result.setProperty("skip", skip.serialize());
    }
    if (limit != null) {
      result.setProperty("limit", limit.serialize());
    }
    if (lockRecord != null) {
      result.setProperty("lockRecord", lockRecord.toString());
    }
    if (fetchPlan != null) {
      result.setProperty("fetchPlan", fetchPlan.serialize());
    }
    if (letClause != null) {
      result.setProperty("letClause", letClause.serialize());
    }
    if (timeout != null) {
      result.setProperty("timeout", timeout.serialize());
    }
    result.setProperty("parallel", parallel);
    result.setProperty("noCache", noCache);
    return result;
  }

  public void deserialize(OResult fromResult) {
    if (fromResult.getProperty("target") != null) {
      target = new OFromClause(-1);
      target.deserialize(fromResult.getProperty("target"));
    }
    if (fromResult.getProperty("projection") != null) {
      projection = new OProjection(-1);
      projection.deserialize(fromResult.getProperty("projection"));
    }
    if (fromResult.getProperty("whereClause") != null) {
      whereClause = new OWhereClause(-1);
      whereClause.deserialize(fromResult.getProperty("whereClause"));
    }
    if (fromResult.getProperty("groupBy") != null) {
      groupBy = new OGroupBy(-1);
      groupBy.deserialize(fromResult.getProperty("groupBy"));
    }
    if (fromResult.getProperty("orderBy") != null) {
      orderBy = new OOrderBy(-1);
      orderBy.deserialize(fromResult.getProperty("orderBy"));
    }
    if (fromResult.getProperty("unwind") != null) {
      unwind = new OUnwind(-1);
      unwind.deserialize(fromResult.getProperty("unwind"));
    }
    if (fromResult.getProperty("skip") != null) {
      skip = new OSkip(-1);
      skip.deserialize(fromResult.getProperty("skip"));
    }
    if (fromResult.getProperty("limit") != null) {
      limit = new OLimit(-1);
      limit.deserialize(fromResult.getProperty("limit"));
    }
    if (fromResult.getProperty("lockRecord") != null) {
      lockRecord = OStorage.LOCKING_STRATEGY.valueOf(fromResult.getProperty("lockRecord"));
    }
    if (fromResult.getProperty("fetchPlan") != null) {
      fetchPlan = new OFetchPlan(-1);
      fetchPlan.deserialize(fromResult.getProperty("fetchPlan"));
    }
    if (fromResult.getProperty("letClause") != null) {
      letClause = new OLetClause(-1);
      letClause.deserialize(fromResult.getProperty("letClause"));
    }
    if (fromResult.getProperty("timeout") != null) {
      timeout = new OTimeout(-1);
      timeout.deserialize(fromResult.getProperty("timeout"));
    }

    parallel = fromResult.getProperty("parallel");
    noCache = fromResult.getProperty("noCache");
  }
}
/* JavaCC - OriginalChecksum=b26959b9726a8cf35d6283eca931da6b (do not edit this line) */
