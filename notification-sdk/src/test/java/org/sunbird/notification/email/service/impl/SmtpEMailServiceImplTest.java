package org.sunbird.notification.email.service.impl;

import com.sun.mail.util.PropUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sunbird.notification.beans.Constants;
import org.sunbird.notification.beans.EmailConfig;
import org.sunbird.notification.beans.EmailRequest;
import org.sunbird.notification.utils.Util;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import java.util.Arrays;
import java.util.HashMap;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
       Util.class,
        Session.class,
        PropUtil.class
})
@PowerMockIgnore({
        "javax.management.*",
        "javax.net.ssl.*",
        "javax.security.*",
        "jdk.internal.reflect.*",
        "javax.crypto.*",
        "javax.script.*",
        "javax.xml.*",
        "com.sun.org.apache.xerces.*",
        "org.xml.*"
})
public class SmtpEMailServiceImplTest {

    @Before
    public void setUp() throws MessagingException {

        PowerMockito.mockStatic(Util.class);
        PowerMockito.when(Util.readValue(Mockito.eq(Constants.EMAIL_SERVER_HOST))).thenReturn("http://localhost:9191");
        PowerMockito.when(Util.readValue(Mockito.eq(Constants.EMAIL_SERVER_PORT))).thenReturn("1234");
        PowerMockito.when(Util.readValue(Mockito.eq(Constants.EMAIL_SERVER_USERNAME))).thenReturn("john12");
        PowerMockito.when(Util.readValue(Mockito.eq(Constants.EMAIL_SERVER_PASSWORD))).thenReturn("123#4343");
        PowerMockito.when(Util.readValue(Mockito.eq(Constants.EMAIL_SERVER_FROM))).thenReturn("info@sunbird.com");


           }


    @Test
    public void testSendEmailWithConfigSuccess() throws MessagingException {
        Session session = mockSession();
        PowerMockito.mockStatic(PropUtil.class);
        PowerMockito.when(PropUtil.getBooleanSessionProperty(Mockito.eq(session),Mockito.eq("mail.mime.address.strict")  ,Mockito.eq(true))).thenReturn(true);
        SmtpEMailServiceImpl smtpEMailService = new SmtpEMailServiceImpl(new EmailConfig());
        Assert.assertTrue(smtpEMailService.sendEmail(getEmailRequest(), new HashMap<>()));
    }

    @Test
    public void testSendEmailSuccess() throws MessagingException {
        Session session = mockSession();
        PowerMockito.mockStatic(PropUtil.class);
        PowerMockito.when(PropUtil.getBooleanSessionProperty(Mockito.eq(session),Mockito.eq("mail.mime.address.strict")  ,Mockito.eq(true))).thenReturn(true);

        SmtpEMailServiceImpl smtpEMailService = new SmtpEMailServiceImpl();
        Assert.assertTrue(smtpEMailService.sendEmail(getEmailRequest(), new HashMap<>()));
    }



    @Test
    public void testMultipleUserSendEmailSuccess() throws MessagingException {
        Session session = mockSession();
        PowerMockito.mockStatic(PropUtil.class);
        PowerMockito.when(PropUtil.getBooleanSessionProperty(Mockito.eq(session),Mockito.eq("mail.mime.address.strict")  ,Mockito.eq(true))).thenReturn(true);

        SmtpEMailServiceImpl smtpEMailService = new SmtpEMailServiceImpl();
        Assert.assertTrue(smtpEMailService.sendEmail(getMultiRecieverEmailRequest(), new HashMap<>()));
    }

    @Test
    public void testSendEmailFailure() throws MessagingException {
        Session session = mockSessionFailure();
        PowerMockito.mockStatic(PropUtil.class);
        PowerMockito.when(PropUtil.getBooleanSessionProperty(Mockito.eq(session),Mockito.eq("mail.mime.address.strict")  ,Mockito.eq(true))).thenReturn(true);
        SmtpEMailServiceImpl smtpEMailService = new SmtpEMailServiceImpl();
        boolean flag = true;
        try {
            flag = smtpEMailService.sendEmail(getEmailRequest(), new HashMap<>());
        }catch (Exception ex){
            flag = false;
        }
        Assert.assertFalse(flag);
    }

    private EmailRequest getMultiRecieverEmailRequest() {
        EmailRequest request = new EmailRequest();
        request.setBody("MESSAGE");
        request.setSubject("HELLO");
        request.setTo(Arrays.asList("test@gmail.com"));
        return request;
    }

    private Session mockSession() throws MessagingException {
        PowerMockito.mockStatic(Session.class);
        Session session = Mockito.mock(Session.class);
        PowerMockito.when(Session.getInstance(Mockito.any(),Mockito.any())).thenReturn(session);
        Transport transport = Mockito.mock(Transport.class);
        Mockito.when(session.getTransport(Mockito.anyString())).thenReturn(transport);
        Mockito.doNothing().when(transport).connect(Mockito.anyString(),Mockito.anyString(),Mockito.anyString());
        Mockito.doNothing().when(transport).sendMessage(Mockito.any(),Mockito.any());
        Mockito.doNothing().when(transport).close();
        return session;
    }

    private Session mockSessionFailure() throws MessagingException {
        PowerMockito.mockStatic(Session.class);
        Session session = Mockito.mock(Session.class);
        PowerMockito.when(Session.getInstance(Mockito.any(),Mockito.any())).thenReturn(session);
        Transport transport = Mockito.mock(Transport.class);
        Mockito.when(session.getTransport(Mockito.anyString())).thenReturn(transport);
        Mockito.doNothing().when(transport).connect(Mockito.anyString(),Mockito.anyString(),Mockito.anyString());
        Mockito.doThrow(new RuntimeException("Error while sending")).when(transport).sendMessage(Mockito.any(),Mockito.any());
        Mockito.doThrow(new RuntimeException("Error while closing")).when(transport).close();
        return session;
    }

    private EmailRequest getEmailRequest() {
        EmailRequest request = new EmailRequest();
        request.setBcc(Arrays.asList("Test"));
        request.setBody("MESSAGE");
        request.setSubject("HELLO");
        request.setTo(Arrays.asList("test@gmail.com"));
        return request;
    }

}
