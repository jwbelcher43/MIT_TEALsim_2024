/* Created by JReleaseInfo AntTask from Open Source Competence Group */
/* Creation date Thu Sep 27 18:31:18 EDT 2007 */
package teal;

import java.util.Date;

/**
 * This class provides information gathered from the build environment.
 * 
 * @author JReleaseInfo AntTask
 */
public class Version {


   /** buildDate (set during build process to 1190932278140L). */
   private static Date buildDate = new Date(1190932278140L);

   /**
    * Get buildDate (set during build process to Thu Sep 27 18:31:18 EDT 2007).
    * @return Date buildDate
    */
   public static final Date getBuildDate() { return buildDate; }


   /** year (set during build process to "2007"). */
   private static String year = new String("2007");

   /**
    * Get year (set during build process to "2007").
    * @return String year
    */
   public static final String getYear() { return year; }


   /** project (set during build process to "TEALsim"). */
   private static String project = new String("TEALsim");

   /**
    * Get project (set during build process to "TEALsim").
    * @return String project
    */
   public static final String getProject() { return project; }


   /** buildTimestamp (set during build process to "09/27/2007 06:31 PM"). */
   private static String buildTimestamp = new String("09/27/2007 06:31 PM");

   /**
    * Get buildTimestamp (set during build process to "09/27/2007 06:31 PM").
    * @return String buildTimestamp
    */
   public static final String getBuildTimestamp() { return buildTimestamp; }


   /** version (set during build process to "v0.3"). */
   private static String version = new String("v0.3");

   /**
    * Get version (set during build process to "v0.3").
    * @return String version
    */
   public static final String getVersion() { return version; }

}
