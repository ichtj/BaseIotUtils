// IPackageDeleteObserver.aidl
package android.content.pm;

/**
 * API for deletion callbacks from the Package Manager.
 *
 */
oneway interface IPackageDeleteObserver {
    void packageDeleted(in String packageName, in int returnCode);
}
