/*
 *
 *  *  Copyright 2014 OrientDB LTD (info(at)orientdb.com)
 *  *
 *  *  Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *
 *  *       http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *  *
 *  * For more information: http://www.orientdb.com
 *
 */
package com.orientechnologies.orient.core.metadata.sequence;

import com.orientechnologies.common.exception.OException;
import com.orientechnologies.common.util.OApi;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.db.ODatabaseDocumentInternal;
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.exception.OConcurrentModificationException;
import com.orientechnologies.orient.core.exception.OSequenceException;
import com.orientechnologies.orient.core.exception.OStorageException;
import com.orientechnologies.orient.core.metadata.schema.OClassImpl;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;

import java.util.Random;
import java.util.concurrent.Callable;

/**
 * @author Matan Shukry (matanshukry@gmail.com)
 * @since 3/2/2015
 */
public abstract class OSequence {
  public static final long    DEFAULT_START            = 0;
  public static final int     DEFAULT_INCREMENT        = 1;
  public static final int     DEFAULT_CACHE            = 20;
  public static final Long    DEFAULT_LIMIT_VALUE      = null;
  public static final boolean DEFAULT_RECYCLABLE_VALUE = false;

  protected static final int    DEF_MAX_RETRY = OGlobalConfiguration.SEQUENCE_MAX_RETRY.getValueAsInteger();
  public static final    String CLASS_NAME    = "OSequence";

  private static final String FIELD_START       = "start";
  private static final String FIELD_INCREMENT   = "incr";
  private static final String FIELD_VALUE       = "value";
  private static final String FIELD_LIMIT_VALUE = "lvalue";
  private static final String FIELD_ORDER_TYPE  = "otype";
  private static final String FIELD_RECYCLABLE  = "recycle";
  //initialy set this value to true, so those one who read it can pull upper limit value from document  

  private static final String FIELD_NAME = "name";
  private static final String FIELD_TYPE = "type";

  private ODocument document;
  private ThreadLocal<ODocument> tlDocument = new ThreadLocal<ODocument>();

  private boolean cruacialValueChanged = false;

  public static final SequenceOrderType DEFAULT_ORDER_TYPE = SequenceOrderType.ORDER_POSITIVE;

  public static class CreateParams {
    public Long              start        = DEFAULT_START;
    public Integer           increment    = DEFAULT_INCREMENT;
    public Integer           cacheSize    = DEFAULT_CACHE;
    public Long              limitValue   = DEFAULT_LIMIT_VALUE;
    public SequenceOrderType orderType    = DEFAULT_ORDER_TYPE;
    public Boolean           recyclable   = DEFAULT_RECYCLABLE_VALUE;
    public Boolean           turnLimitOff = false;

    public CreateParams setStart(Long start) {
      this.start = start;
      return this;
    }

    public CreateParams setIncrement(Integer increment) {
      this.increment = increment;
      return this;
    }

    public CreateParams setCacheSize(Integer cacheSize) {
      this.cacheSize = cacheSize;
      return this;
    }

    public CreateParams setLimitValue(Long limitValue) {
      this.limitValue = limitValue;
      return this;
    }

    public CreateParams setOrderType(SequenceOrderType orderType) {
      this.orderType = orderType;
      return this;
    }

    public CreateParams setRecyclable(boolean recyclable) {
      this.recyclable = recyclable;
      return this;
    }

    public CreateParams setTurnLimitOff(Boolean turnLimitOff) {
      this.turnLimitOff = turnLimitOff;
      return this;
    }

    public CreateParams() {
    }

    public CreateParams resetNull() {
      start = null;
      increment = null;
      cacheSize = null;
      limitValue = null;
      orderType = null;
      recyclable = null;
      turnLimitOff = false;
      return this;
    }

    public CreateParams setDefaults() {
      this.start = this.start != null ? this.start : DEFAULT_START;
      this.increment = this.increment != null ? this.increment : DEFAULT_INCREMENT;
      this.cacheSize = this.cacheSize != null ? this.cacheSize : DEFAULT_CACHE;
      limitValue = limitValue == null ? DEFAULT_LIMIT_VALUE : limitValue;
      orderType = orderType == null ? DEFAULT_ORDER_TYPE : orderType;
      recyclable = recyclable == null ? DEFAULT_RECYCLABLE_VALUE : recyclable;
      turnLimitOff = turnLimitOff == null ? false : turnLimitOff;

      return this;
    }
  }

  public enum SEQUENCE_TYPE {
    CACHED, ORDERED,;
  }

  private int maxRetry = DEF_MAX_RETRY;

  protected OSequence() {
    this(null, null);
  }

  protected void setCrucialValueChanged(boolean val) {
    synchronized (this) {
      cruacialValueChanged = val;
    }
  }

  protected boolean getCrucilaValueChanged() {
    synchronized (this) {
      return cruacialValueChanged;
    }
  }

  protected OSequence(final ODocument iDocument) {
    this(iDocument, null);
  }

  protected OSequence(final ODocument iDocument, CreateParams params) {
    document = iDocument != null ? iDocument : new ODocument(CLASS_NAME);
    bindOnLocalThread();

    if (iDocument == null) {
      if (params == null) {
        params = new CreateParams().setDefaults();
      }

      initSequence(params);
      document = getDocument();
    }
    cruacialValueChanged = true;
  }

  public void save() {
    ODocument doc = tlDocument.get();
    doc.save();
    onUpdate(doc);
  }

  public void save(ODatabaseDocument database) {
    database.save(tlDocument.get());
  }

  void bindOnLocalThread() {
    if (tlDocument.get() == null) {
      tlDocument.set(document.copy());
    }
  }

  public ODocument getDocument() {
    return tlDocument.get();
  }

  protected synchronized void initSequence(OSequence.CreateParams params) {
    setStart(params.start);
    setIncrement(params.increment);
    setValue(params.start);
    setLimitValue(params.limitValue);
    setOrderType(params.orderType);
    setRecyclable(params.recyclable);

    setSequenceType();
  }

  public synchronized boolean updateParams(CreateParams params) {
    boolean any = false;

    if (params.start != null && this.getStart() != params.start) {
      this.setStart(params.start);
      any = true;
    }

    if (params.increment != null && this.getIncrement() != params.increment) {
      this.setIncrement(params.increment);
      any = true;
    }

    if (params.limitValue != null && this.getLimitValue() != params.limitValue) {
      this.setLimitValue(params.limitValue);
      any = true;
    }

    if (params.orderType != null && this.getOrderType() != params.orderType) {
      this.setOrderType(params.orderType);
      any = true;
    }

    if (params.recyclable != null && this.getRecyclable() != params.recyclable) {
      this.setRecyclable(params.recyclable);
      any = true;
    }

    if (params.turnLimitOff != null && params.turnLimitOff == true) {
      this.setLimitValue(null);
    }

    save();
    reset();

    return any;
  }

  public void onUpdate(ODocument iDocument) {
    document = iDocument;
    this.tlDocument.set(iDocument);
  }

  protected static Long getValue(ODocument doc) {
    if (!doc.containsField(FIELD_VALUE)) {
      return null;
    }
    return doc.field(FIELD_VALUE, OType.LONG);
  }

  protected synchronized Long getValue() {
    return getValue(tlDocument.get());
  }

  protected synchronized void setValue(long value) {
    tlDocument.get().field(FIELD_VALUE, value);
    setCrucialValueChanged(true);
  }

  protected synchronized int getIncrement() {
    return tlDocument.get().field(FIELD_INCREMENT, OType.INTEGER);
  }

  protected synchronized void setLimitValue(Long limitValue) {
    tlDocument.get().field(FIELD_LIMIT_VALUE, limitValue);
    setCrucialValueChanged(true);
  }

  protected synchronized Long getLimitValue() {
    return tlDocument.get().field(FIELD_LIMIT_VALUE, OType.LONG);
  }

  protected synchronized void setOrderType(SequenceOrderType orderType) {
    tlDocument.get().field(FIELD_ORDER_TYPE, orderType.getValue());
    setCrucialValueChanged(true);
  }

  protected synchronized SequenceOrderType getOrderType() {
    byte val = tlDocument.get().field(FIELD_ORDER_TYPE);
    return SequenceOrderType.fromValue(val);
  }

  protected synchronized void setIncrement(int value) {
    tlDocument.get().field(FIELD_INCREMENT, value);
    setCrucialValueChanged(true);
  }

  protected synchronized long getStart() {
    return tlDocument.get().field(FIELD_START, OType.LONG);
  }

  protected synchronized void setStart(long value) {
    tlDocument.get().field(FIELD_START, value);
    setCrucialValueChanged(true);
  }

  public synchronized int getMaxRetry() {
    return maxRetry;
  }

  public synchronized void setMaxRetry(final int maxRetry) {
    this.maxRetry = maxRetry;
  }

  public synchronized String getName() {
    return getSequenceName(tlDocument.get());
  }

  public synchronized OSequence setName(final String name) {
    tlDocument.get().field(FIELD_NAME, name);
    return this;
  }

  public synchronized boolean getRecyclable() {
    return tlDocument.get().field(FIELD_RECYCLABLE, OType.BOOLEAN);
  }

  public synchronized void setRecyclable(final boolean recyclable) {
    tlDocument.get().field(FIELD_RECYCLABLE, recyclable);
    setCrucialValueChanged(true);
  }

  private synchronized void setSequenceType() {
    tlDocument.get().field(FIELD_TYPE, getSequenceType());
    setCrucialValueChanged(true);
  }

  protected synchronized ODatabaseDocumentInternal getDatabase() {
    return ODatabaseRecordThreadLocal.instance().get();
  }

  public static String getSequenceName(final ODocument iDocument) {
    return iDocument.field(FIELD_NAME, OType.STRING);
  }

  public static SEQUENCE_TYPE getSequenceType(final ODocument document) {
    String sequenceTypeStr = document.field(FIELD_TYPE);
    if (sequenceTypeStr != null)
      return SEQUENCE_TYPE.valueOf(sequenceTypeStr);

    return null;
  }

  public static void initClass(OClassImpl sequenceClass) {
    sequenceClass.createProperty(OSequence.FIELD_START, OType.LONG, (OType) null, true);
    sequenceClass.createProperty(OSequence.FIELD_INCREMENT, OType.INTEGER, (OType) null, true);
    sequenceClass.createProperty(OSequence.FIELD_VALUE, OType.LONG, (OType) null, true);

    sequenceClass.createProperty(OSequence.FIELD_NAME, OType.STRING, (OType) null, true);
    sequenceClass.createProperty(OSequence.FIELD_TYPE, OType.STRING, (OType) null, true);

    sequenceClass.createProperty(OSequence.FIELD_LIMIT_VALUE, OType.INTEGER, (OType) null, true);
    sequenceClass.createProperty(OSequence.FIELD_ORDER_TYPE, OType.BYTE, (OType) null, true);
    sequenceClass.createProperty(OSequence.FIELD_RECYCLABLE, OType.BOOLEAN, (OType) null, true);
  }

  /*
   * Forwards the sequence by one, and returns the new value.
   */
  @OApi
  public abstract long next() throws OSequenceLimitReachedException;

  /*
   * Returns the current sequence value. If next() was never called, returns null
   */
  @OApi
  public abstract long current();

  /*
   * Resets the sequence value to it's initialized value.
   */
  @OApi
  public abstract long reset();

  /*
   * Returns the sequence type
   */
  public abstract SEQUENCE_TYPE getSequenceType();

  protected void reloadSequence() {
    tlDocument.set(tlDocument.get().reload(null, true));
  }

  protected <T> T callRetry(final Callable<T> callable, final String method) {
    for (int retry = 0; retry < maxRetry; ++retry) {
      try {
        reloadSequence();
        return callable.call();
      } catch (OConcurrentModificationException ignore) {
        try {
          Thread.sleep(1 + new Random()
              .nextInt(getDatabase().getConfiguration().getValueAsInteger(OGlobalConfiguration.SEQUENCE_RETRY_DELAY)));
        } catch (InterruptedException ignored) {
          Thread.currentThread().interrupt();
          break;
        }

      } catch (OStorageException e) {
        if (e.getCause() instanceof OConcurrentModificationException) {
          reloadSequence();
        } else {
          throw OException
              .wrapException(new OSequenceException("Error in transactional processing of " + getName() + "." + method + "()"), e);
        }
      } catch (OSequenceLimitReachedException exc) {
        throw exc;
      } catch (OException ignore) {
        reloadSequence();
      } catch (Exception e) {
        throw OException
            .wrapException(new OSequenceException("Error in transactional processing of " + getName() + "." + method + "()"), e);
      }
    }

    try {
      return callable.call();
    } catch (Exception e) {
      if (e.getCause() instanceof OConcurrentModificationException) {
        //noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
        throw ((OConcurrentModificationException) e.getCause());
      }
      throw OException
          .wrapException(new OSequenceException("Error in transactional processing of " + getName() + "." + method + "()"), e);
    }
  }
}
