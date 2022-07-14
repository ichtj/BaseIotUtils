// IPackageDeleteObserver.aidl
package android.content.pm;

/**
 * API for installation callbacks from the Package Manager.
 * @hide
 */
oneway interface IPackageInstallObserver {
    void packageInstalled(in String packageName, int returnCode);
}
