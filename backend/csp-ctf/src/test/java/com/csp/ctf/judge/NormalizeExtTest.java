package com.csp.ctf.judge;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/** 校验 CtfChallengeController.normalizeExt 的容器/附件分流回填。 */
class NormalizeExtTest {

    private String call(String ext, String attachment, String imageName) throws Exception {
        Class<?> c = Class.forName("com.csp.ctf.controller.CtfChallengeController");
        Method m = c.getDeclaredMethod("normalizeExt", String.class, String.class, String.class);
        m.setAccessible(true);
        Object inst = com.sun.tools.javac.Main.class == null ? null : null;
        // 用 Unsafe 绕开构造：normalizeExt 不依赖实例字段
        sun.misc.Unsafe u = null;
        try {
            java.lang.reflect.Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            u = (sun.misc.Unsafe) f.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Object target = u.allocateInstance(c);
        return (String) m.invoke(target, ext, attachment, imageName);
    }

    @Test
    void onlyImageName_shouldMarkDocker() throws Exception {
        String json = call(null, null, "csp/web-php:latest");
        QuestionExt ext = QuestionExt.parse(json);
        assertTrue(ext.dockerEnabled(), "只传 imageName 应回填 isDocker=true, json=" + json);
        assertEquals("csp/web-php:latest", ext.getImageName());
    }

    @Test
    void onlyAttachment_shouldNotMarkDocker() throws Exception {
        String json = call(null, "/files/a.zip", null);
        QuestionExt ext = QuestionExt.parse(json);
        assertFalse(ext.dockerEnabled(), "只传附件不应是容器题, json=" + json);
        assertEquals("/files/a.zip", ext.getAttachmentUrl());
    }
}
