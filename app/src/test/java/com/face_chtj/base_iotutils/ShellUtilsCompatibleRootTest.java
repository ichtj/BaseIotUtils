package com.face_chtj.base_iotutils;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ShellUtilsCompatibleRootTest {
    @Test
    public void rootIdentityAcceptsAndroidIdOutput() {
        ShellUtils.CommandResult result = success("uid=0(root) gid=0(root) groups=0(root)");

        assertTrue(ShellUtils.isRootIdentity(result));
    }

    @Test
    public void rootIdentityAcceptsPromptBeforeIdOutput() {
        ShellUtils.CommandResult result = success("root-shell\nuid=0 root gid=0");

        assertTrue(ShellUtils.isRootIdentity(result));
    }

    @Test
    public void rootIdentityRejectsShellUser() {
        ShellUtils.CommandResult result = success("uid=2000(shell) gid=2000(shell)");

        assertFalse(ShellUtils.isRootIdentity(result));
    }

    @Test
    public void rootIdentityRejectsFailedCommand() {
        ShellUtils.CommandResult result = success("uid=0(root) gid=0(root)");
        result.result = ShellUtils.RESULT_EXCEPTION;

        assertFalse(ShellUtils.isRootIdentity(result));
    }

    @Test
    public void rootIdentityRejectsMissingOutput() {
        ShellUtils.CommandResult result = success(null);

        assertFalse(ShellUtils.isRootIdentity(result));
        assertFalse(ShellUtils.isRootIdentity(null));
    }

    private static ShellUtils.CommandResult success(String output) {
        ShellUtils.CommandResult result = new ShellUtils.CommandResult();
        result.result = ShellUtils.RESULT_SUCCESS;
        result.successMsg = output;
        return result;
    }
}
