/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.core.util;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.collections.Predicate;

/**
 * @author <a href="mailto:thomas.hoedl@aschauer-edv.at">Thomas Hoedl(asc042)</a>
 * @version $Revision
 *
 */
public final class CollectionPredicates { // NOPMD

  private static final String TO_STRING_PREFIX = CollectionPredicates.class.getName() + ".";

  /**
   * Empty collection.
   *
   */
  public static final Predicate EMPTY_COLLECTION = new Predicate() {

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.collections.Predicate#evaluate(java.lang.Object)
     */
    public boolean evaluate( final Object object ) {
      return ( (Collection<?>) object ).isEmpty();
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return TO_STRING_PREFIX + "EMPTY_COLLECTION";
    }

  };

  /**
   * Not empty collection.
   */
  public static final Predicate NOT_EMPTY_COLLECTION = new Predicate() {

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.collections.Predicate#evaluate(java.lang.Object)
     */
    public boolean evaluate( final Object object ) {
      return !EMPTY_COLLECTION.evaluate( object );
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return TO_STRING_PREFIX + "NOT_EMPTY_COLLECTION";
    }

  };

  /**
   * Empty array.
   */
  public static final Predicate EMPTY_ARRAY = new Predicate() {

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.collections.Predicate#evaluate(java.lang.Object)
     */
    public boolean evaluate( final Object object ) {
      return ( (Object[]) object ).length == 0;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return TO_STRING_PREFIX + "EMPTY_ARRAY";
    }
  };

  /**
   * Not empty array.
   */
  public static final Predicate NOT_EMPTY_ARRAY = new Predicate() {
    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.collections.Predicate#evaluate(java.lang.Object)
     */
    public boolean evaluate( final Object object ) {
      return !EMPTY_ARRAY.evaluate( object );
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return TO_STRING_PREFIX + "NOT_EMPTY_ARRAY";
    }
  };

  /**
   * Empty map.
   */
  public static final Predicate EMPTY_MAP = new Predicate() {

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.collections.Predicate#evaluate(java.lang.Object)
     */
    public boolean evaluate( final Object object ) {
      return ( (Map<?, ?>) object ).isEmpty();
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return TO_STRING_PREFIX + "EMPTY_MAP";
    }
  };

  /**
   * Not empty map.
   *
   */
  public static final Predicate NOT_EMPTY_MAP = new Predicate() {
    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.collections.Predicate#evaluate(java.lang.Object)
     */
    public boolean evaluate( final Object object ) {
      return !EMPTY_MAP.evaluate( object );
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return TO_STRING_PREFIX + "NOT_EMPTY_MAP";
    }
  };

  /**
   * Not null or empty collection.
   */
  public static final Predicate NOT_NULL_OR_EMPTY_COLLECTION = new Predicate() {

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.collections.Predicate#evaluate(java.lang.Object)
     */
    public boolean evaluate( final Object object ) {
      return object != null && !EMPTY_COLLECTION.evaluate( object );
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return TO_STRING_PREFIX + "NOT_NULL_OR_EMPTY_COLLECTION";
    }

  };

  /**
   * Not null or empty array.
   */
  public static final Predicate NOT_NULL_OR_EMPTY_ARRAY = new Predicate() {
    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.collections.Predicate#evaluate(java.lang.Object)
     */
    public boolean evaluate( final Object object ) {
      return object != null && !EMPTY_ARRAY.evaluate( object );
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return TO_STRING_PREFIX + "NOT_NULL_OR_EMPTY_ARRAY";
    }
  };

  /**
   * Not null or empty map.
   *
   *
   */
  public static final Predicate NOT_NULL_OR_EMPTY_MAP = new Predicate() {
    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.collections.Predicate#evaluate(java.lang.Object)
     */
    public boolean evaluate( final Object object ) {
      return object != null && !EMPTY_MAP.evaluate( object );
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return TO_STRING_PREFIX + "NOT_NULL_OR_EMPTY_MAP";
    }
  };

  /**
   * Null or empty collection.
   */
  public static final Predicate NULL_OR_EMPTY_COLLECTION = new Predicate() {
    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.collections.Predicate#evaluate(java.lang.Object)
     */
    public boolean evaluate( final Object object ) {
      return object == null || EMPTY_COLLECTION.evaluate( object );
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return TO_STRING_PREFIX + "NULL_OR_EMPTY_COLLECTION";
    }
  };

  /**
   * Null or empty array.
   */
  public static final Predicate NULL_OR_EMPTY_ARRAY = new Predicate() {
    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.collections.Predicate#evaluate(java.lang.Object)
     */
    public boolean evaluate( final Object object ) {
      return object == null || EMPTY_ARRAY.evaluate( object );
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return TO_STRING_PREFIX + "NULL_OR_EMPTY_ARRAY";
    }
  };

  /**
   * Null or empty map.
   */
  public static final Predicate NULL_OR_EMPTY_MAP = new Predicate() {
    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.collections.Predicate#evaluate(java.lang.Object)
     */
    public boolean evaluate( final Object object ) {
      return object == null || EMPTY_MAP.evaluate( object );
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return TO_STRING_PREFIX + "NULL_OR_EMPTY_MAP";
    }
  };

  /**
   * Avoid direct initialization.
   */
  private CollectionPredicates() {
    super();
  }

  /**
   * @param subject
   *          the subject.
   * @return true if null or empty.
   */
  public static boolean isNullOrEmpty( final Collection<?> subject ) {
    return NULL_OR_EMPTY_COLLECTION.evaluate( subject );
  }

  /**
   * @param subject
   *          the subject.
   * @return true if not null or empty.
   */
  public static boolean isNotNullOrEmpty( final Collection<?> subject ) {
    return NOT_NULL_OR_EMPTY_COLLECTION.evaluate( subject );
  }

  /**
   * @param subject
   *          the subject.
   * @return true if null or empty.
   *
   */
  public static boolean isNullOrEmpty( final Object[] subject ) {
    return NULL_OR_EMPTY_ARRAY.evaluate( subject );
  }

  /**
   * @param subject
   *          the subject.
   * @return true if not null or empty.
   *
   */
  public static boolean isNotNullOrEmpty( final Object[] subject ) {
    return NOT_NULL_OR_EMPTY_ARRAY.evaluate( subject );
  }

  /**
   * @param subject
   *          the subject.
   * @return true if null or empty.
   *
   */
  public static boolean isNullOrEmpty( final Map<?, ?> subject ) {
    return NULL_OR_EMPTY_MAP.evaluate( subject );
  }

  /**
   * @param subject
   *          the subject.
   * @return true if not null or empty.
   *
   */
  public static boolean isNotNullOrEmpty( final Map<?, ?> subject ) {
    return NOT_NULL_OR_EMPTY_MAP.evaluate( subject );
  }

}
