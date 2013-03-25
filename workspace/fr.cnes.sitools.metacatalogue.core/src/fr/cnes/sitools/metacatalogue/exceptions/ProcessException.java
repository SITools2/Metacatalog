/*******************************************************************************
 * Copyright 2011 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 * 
 * This file is part of SITools2.
 * 
 * SITools2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * SITools2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SITools2.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.cnes.sitools.metacatalogue.exceptions;

/**
 * Exception that can be thrown during the harvesting process
 * 
 * @author m.gond
 * 
 */
public class ProcessException extends Exception {
  /** serialVersionUID */
  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new ProcessException with null as its detail message.
   */
  public ProcessException() {
    super();
  }

  /**
   * Constructs a new ProcessException with the specified detail message. The cause is not initialized, and may
   * subsequently be initialized by a call to Throwable.initCause(java.lang.Throwable).
   * 
   * @param message
   *          - the detail message (which is saved for later retrieval by the Throwable.getMessage() method). cause -
   *          the
   * @param cause
   *          (which is saved for later retrieval by the Throwable.getCause() method). (A null value is permitted, and
   *          indicates that the cause is nonexistent or unknown.)
   */
  public ProcessException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new ProcessException with the specified detail message. The cause is not initialized, and may
   * subsequently be initialized by a call to Throwable.initCause(java.lang.Throwable). Parameters:
   * 
   * @param message
   *          the detail message. The detail message is saved for later retrieval by the Throwable.getMessage() method.
   */
  public ProcessException(String message) {
    super(message);
  }

  /**
   * Constructs a new ProcessException with the specified cause and a detail message of (cause==null ? null :
   * cause.toString()) (which typically contains the class and detail message of cause). This constructor is useful for
   * exceptions that are little more than wrappers for other throwables (for example, PrivilegedActionException).
   * 
   * @param cause
   *          the cause (which is saved for later retrieval by the Throwable.getCause() method). (A null value is
   *          permitted, and indicates that the cause is nonexistent or unknown.)
   */
  public ProcessException(Throwable cause) {
    super(cause);
  }
}
