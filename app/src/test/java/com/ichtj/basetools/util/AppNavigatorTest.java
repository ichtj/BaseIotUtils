package com.ichtj.basetools.util;

import com.ichtj.basetools.BuildConfig;

import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import static org.junit.Assert.assertTrue;

public class AppNavigatorTest {
    @Test
    public void generatedEntryIsAnEnabledManifestActivity() throws Exception {
        Path root = Paths.get("").toAbsolutePath();
        if (!Files.isRegularFile(root.resolve("config.gradle"))) {
            root = root.getParent();
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Element manifest = factory.newDocumentBuilder().parse(
                root.resolve("app/src/main/AndroidManifest.xml").toFile()).getDocumentElement();
        String packageName = manifest.getAttribute("package");
        String androidNamespace = "http://schemas.android.com/apk/res/android";
        Set<String> declaredActivities = new HashSet<>();
        NodeList activities = manifest.getElementsByTagName("activity");
        for (int i = 0; i < activities.getLength(); i++) {
            Element activity = (Element) activities.item(i);
            if ("false".equals(activity.getAttributeNS(androidNamespace, "enabled"))) {
                continue;
            }
            String name = activity.getAttributeNS(androidNamespace, "name");
            if (name.startsWith(".")) {
                name = packageName + name;
            } else if (!name.contains(".")) {
                name = packageName + "." + name;
            }
            declaredActivities.add(name);
        }

        String target = BuildConfig.ENTRY_ACTIVITY.getName();
        assertTrue(BuildConfig.APP_CHOOSE + " must target an enabled manifest Activity: " + target,
                declaredActivities.contains(target));
    }
}
