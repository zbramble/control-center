package com.wenba.scheduler.email;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.mail.imap.IMAPMessage;
import com.wenba.scheduler.ISchedulerStrategy;
import com.wenba.scheduler.SchedulerConstants;
import com.wenba.scheduler.SchedulerControllerStatistics;
import com.wenba.scheduler.config.ConfigParam;
import com.wenba.scheduler.config.ConfigParam.ConfigFileType;
import com.wenba.scheduler.config.ConfigResult;
import com.wenba.scheduler.config.SchedulerConfiguration;

/**
 * @author zhangbo
 *
 */
public class EmailUtil {

    // constants
    private static final String DEFAULT_CHARSET = "UTF-8";
    private static final int PORT_465 = 465;
    private static final int PORT_993 = 993;
    private static final int CONFIG_EMAIL_SUBJECT_PART_NUM = 4;

    // 成员变量
    /**
     * smtp mail host(sender)
     */
    private String mailHostSmtp = "";

    /**
     * smtp mail port
     */
    private int mailPortSmtp = PORT_465;

    /**
     * imap mail host(receiver)
     */
    private String mailHostImap = "";

    /**
     * imap mail port
     */
    private int mailPortImap = PORT_993;

    /**
     * encryption type
     */
    private int encryptionType = EncryptionTypes.Default.ordinal();

    /**
     * auth
     */
    private boolean auth = false;

    /**
     * mail host account
     */
    private String mailHostAccount = "";

    /**
     * mail host password
     */
    private String mailHostPassword = "";

    /**
     * config files mail recipients
     */
    private String configFilesMailRecipients;

    /**
     * cnn servers mail recipients
     */
    private String cnnServersMailRecipients;

    /**
     * search servers mail recipients
     */
    private String searchServersMailRecipients;

    /**
     * nlp servers mail recipients
     */
    private String nlpServersMailRecipients;

    /**
     * config files mail senders
     */
    private String configFilesMailSenders;

    private static Logger logger = LogManager.getLogger(EmailUtil.class);

    /**
     * Send email to a single recipient or recipient string.
     * 
     * @param emailParam
     *            Email Param
     */
    public void sendEmail(EmailParam emailParam) {
        String senderName = emailParam.getSchedulerId();
        String receiverAddress = emailParam.getReceiverAddress();
        String sub = emailParam.getSub();
        String msg = emailParam.getMsg();
        String[] address = receiverAddress.split(";");
        List<String> recipients = new ArrayList<String>();
        for (int i = 0; i < address.length; i++) {
            if (address[i].trim().length() > 0) {
                recipients.add(address[i]);
            }
        }

        logger.info("mail subject: {}, recipients: {}", sub, receiverAddress);

        Transport transport = null;
        try {
            Properties props = this.getSmtpProperties();
            Session session = this.getSession(props);
            MimeMessage message = new MimeMessage(session);

            message.addHeader("Content-type", "text/plain");
            message.setSubject(sub, DEFAULT_CHARSET);
            message.setFrom(new InternetAddress(this.mailHostAccount,
                    senderName));

            for (String recipient : recipients) {
                message.addRecipients(Message.RecipientType.TO, recipient);
            }

            Multipart mp = new MimeMultipart();

            // content
            MimeBodyPart contentPart = new MimeBodyPart();
            contentPart.setText(msg, DEFAULT_CHARSET);
            mp.addBodyPart(contentPart);
            message.setContent(mp);
            message.setSentDate(new Date());

            if (this.getDefaultEncryptionType() == EncryptionTypes.SSL
                    .ordinal()) {
                Transport.send(message);
            } else {
                transport = session.getTransport("smtp");
                transport.connect(this.mailHostSmtp, this.mailPortSmtp,
                        this.mailHostAccount, this.mailHostPassword);
                transport.sendMessage(message, message.getAllRecipients());
            }
        } catch (Exception e) {
            logger.error("send mail error!", e);
        } finally {
            if (transport != null) {
                try {
                    transport.close();
                } catch (Exception ex) {
                    logger.error("close transport error!", ex);
                }
            }
        }

    }

    /**
     * receive config email
     * 
     * @param emailParam
     *            Email Param
     */
    public void receiveConfigEmail(EmailParam emailParamReceive) {
        SchedulerConfiguration schedulerConfiguration = emailParamReceive
                .getSchedulerConfiguration();
        SchedulerControllerStatistics schedulerControllerStatistics = emailParamReceive
                .getSchedulerControllerStatistics();
        ISchedulerStrategy<ConfigParam, ConfigResult> configStrategy = emailParamReceive
                .getConfigStrategy();
        String[] address = schedulerConfiguration.getEmailUtil()
                .getConfigFilesMailSenders().split(";");
        if (address.length <= 0) {
            return;
        }
        List<String> recipients = new ArrayList<String>();
        for (String recipient : address) {
            if (recipient.trim().length() > 0) {
                recipients.add(recipient);
            }
        }
        Store store = null;
        Folder inbox = null;
        Message[] messages = null;
        boolean isSuccess = true;
        String errorMessage = null;
        String meErrorMessage = "";

        IMAPMessage msg;
        try {
            // 获取收件箱邮件
            Properties props = this.getImapProperties();
            Session session = this.getSession(props);
            store = session.getStore("imap");
            meErrorMessage = "connect mail box fail!";
            store.connect(this.mailHostImap, this.mailPortImap,
                    this.mailHostAccount, this.mailHostPassword);
            meErrorMessage = "get Inbox fail!";
            inbox = store.getFolder("Inbox");
            meErrorMessage = "open Inbox fail!";
            inbox.open(Folder.READ_WRITE);
            FetchProfile profile = new FetchProfile();
            profile.add(FetchProfile.Item.ENVELOPE);
            profile.add(FetchProfile.Item.FLAGS);
            meErrorMessage = "get Inbox message fail!";
            messages = inbox.getMessages();
            meErrorMessage = "fetch Inbox message fail!";
            inbox.fetch(messages, profile);

            // 解析邮件subject，看是否是发给本机的，是的话处理完后删除该邮件
            msgLabel: for (Message message : messages) {
                msg = (IMAPMessage) message;
                meErrorMessage = "get message from fail!";
                InternetAddress ia = new InternetAddress(
                        msg.getFrom()[0].toString());

                // 判断发件人是否有效
                boolean isValidRecipient = false;
                for (String recipient : recipients) {
                    if (recipient.equals(ia.getAddress())) {
                        isValidRecipient = true;
                        break;
                    }
                }
                if (!isValidRecipient) {
                    continue msgLabel;
                }

                // 判断邮件subject是否有效
                meErrorMessage = "get message subject fail!";
                String subject = msg.getSubject();
                logger.info("original subject: {}", subject);
                String[] subjects = null;
                if (subject != null && !"".equals(subject)) {
                    subjects = subject.split(" ");
                    if (subjects.length == CONFIG_EMAIL_SUBJECT_PART_NUM) {
                        if (!EmailType.CONFIG.getValue().equals(subjects[0])) {
                            // subjects[0]不是config
                            continue msgLabel;
                        }

                        if (schedulerConfiguration.getIpList() == null
                                || !schedulerConfiguration.getIpList()
                                        .contains(subjects[1])) {
                            // 本机ip list为空或者subjects[1]不在本机ip list里
                            continue msgLabel;
                        }

                        if (schedulerConfiguration.getName() == null
                                || !schedulerConfiguration.getName().equals(
                                        subjects[2])) {
                            // 本机host为空或者subjects[2]不是本机host
                            continue msgLabel;
                        }

                        if (!schedulerConfiguration
                                .getConfigFileNames()
                                .contains(
                                        subjects[CONFIG_EMAIL_SUBJECT_PART_NUM - 1])) {
                            // subjects[3]不在config file names里
                            continue msgLabel;
                        }
                    } else {
                        // subject组成数量不对
                        continue msgLabel;
                    }
                } else {
                    // subject为空
                    continue msgLabel;
                }

                meErrorMessage = "set message flag fail!";
                msg.setFlag(Flags.Flag.DELETED, true);
                meErrorMessage = "get message content type fail!";
                if (msg.getContent() instanceof Multipart) {
                    meErrorMessage = "get message content fail!";
                    Multipart mp = (Multipart) msg.getContent();
                    meErrorMessage = "get multipart count fail!";
                    for (int i = 0; i < mp.getCount(); ++i) {
                        meErrorMessage = "get multipart body fail!";
                        Part part = mp.getBodyPart(i);
                        meErrorMessage = "get part mimetype fail!";
                        if (part.isMimeType("text/plain")) {
                            meErrorMessage = "get part content fail!";
                            String content = part.getContent().toString()
                                    .trim();
                            logger.info("subject: {} {} {} {}", subjects[0],
                                    subjects[1], subjects[2],
                                    subjects[CONFIG_EMAIL_SUBJECT_PART_NUM - 1]);
                            logger.info("content: {}", content);
                            // 判断内容是否为json
                            JSONObject configData = null;
                            if (!"".equals(content)) {
                                try {
                                    configData = JSONObject.fromObject(content);
                                } catch (JSONException e) {
                                    logger.error("config mail JE!");
                                    isSuccess = false;
                                    errorMessage = "config mail json exception!";
                                    break msgLabel;
                                }
                            }

                            ConfigParam configParam = new ConfigParam();
                            configParam
                                    .setSchedulerConfiguration(schedulerConfiguration);
                            configParam
                                    .setSchedulerControllerStatistics(schedulerControllerStatistics);
                            ConfigFileType configFileType;
                            switch (subjects[CONFIG_EMAIL_SUBJECT_PART_NUM - 1]) {
                            case SchedulerConstants.MAIL_CONFIGURATION:
                                configFileType = ConfigFileType.MAIL_CONFIGURATION;
                                break;
                            case SchedulerConstants.SYSTEM_DATA:
                                configFileType = ConfigFileType.SYSTEM_DATA;
                                break;
                            case SchedulerConstants.SYSTEM_SWITCH:
                                configFileType = ConfigFileType.SYSTEM_SWITCH;
                                break;
                            case SchedulerConstants.DEBUG_SWITCH:
                                configFileType = ConfigFileType.DEBUG_SWITCH;
                                break;
                            case SchedulerConstants.ACCESS_SERVERS:
                                configFileType = ConfigFileType.ACCESS_SERVERS;
                                break;
                            case SchedulerConstants.CNN_SERVERS:
                                configFileType = ConfigFileType.CNN_SERVERS;
                                break;
                            case SchedulerConstants.JAVA_SERVERS:
                                configFileType = ConfigFileType.JAVA_SERVERS;
                                break;
                            case SchedulerConstants.SEARCH_SERVERS:
                                configFileType = ConfigFileType.SEARCH_SERVERS;
                                break;
                            case SchedulerConstants.SEARCH_HOMEWORK_SERVERS:
                                configFileType = ConfigFileType.SEARCH_HOMEWORK_SERVERS;
                                break;
                            case SchedulerConstants.SEARCH_ARTICLE_SERVERS:
                                configFileType = ConfigFileType.SEARCH_ARTICLE_SERVERS;
                                break;
                            case SchedulerConstants.NLP_SERVERS:
                                configFileType = ConfigFileType.NLP_SERVERS;
                                break;
                            case SchedulerConstants.BI_SERVERS:
                                configFileType = ConfigFileType.BI_SERVERS;
                                break;
                            case SchedulerConstants.TIMEOUT_DATA:
                                configFileType = ConfigFileType.TIMEOUT_DATA;
                                break;
                            default:
                                configFileType = ConfigFileType.ALL;
                                break;
                            }
                            configParam.setConfigFileType(configFileType);
                            configParam.setConfigData(configData);
                            configStrategy.excute(configParam);
                            break msgLabel;

                        }
                    }
                }
            }
        } catch (NoSuchProviderException e) {
            isSuccess = false;
            errorMessage = "no such provider exception!";
            logger.error("no such provider exception!", e);
        } catch (AddressException e) {
            isSuccess = false;
            errorMessage = "address exception!";
            logger.error("address exception!", e);
        } catch (MessagingException e) {
            isSuccess = false;
            errorMessage = meErrorMessage;
            logger.error("messaging exception!", e);
        } catch (IOException e) {
            isSuccess = false;
            errorMessage = "IO exception!";
            logger.error("IO exception!", e);
        } finally {
            if (!isSuccess && schedulerConfiguration.isMailOnSwitch()) {
                String ip = schedulerConfiguration.getIp() != null ? schedulerConfiguration
                        .getIp() : "";
                String name = schedulerConfiguration.getName() != null ? schedulerConfiguration
                        .getName() : "";

                List<String> ipList = schedulerConfiguration.getIpList();
                String ipListStr = "";
                if (ipList != null) {
                    for (String ipStr : ipList) {
                        ipListStr += ("      " + ipStr);
                    }
                }

                EmailParam emailParamSend = new EmailParam();
                emailParamSend.setSchedulerId(schedulerConfiguration
                        .getSystemDataConfiguration().getSchedulerId());
                emailParamSend.setReceiverAddress(schedulerConfiguration
                        .getEmailUtil().getConfigFilesMailSenders());
                emailParamSend.setSub("config exception, ip: " + ip
                        + ", name: " + name);
                emailParamSend
                        .setMsg("config exception!\n\n" + "local IP list: "
                                + ipListStr + "\n\n" + errorMessage);
                schedulerConfiguration.getEmailUtil().sendEmail(emailParamSend);
            }
            try {
                if (inbox != null) {
                    inbox.close(true);
                }
            } catch (MessagingException e) {
                logger.error("close inbox error!", e);
            }

            try {
                if (store != null) {
                    store.close();
                }
            } catch (MessagingException e) {
                logger.error("close store error!", e);
            }
        }
    }

    private Properties getSmtpProperties() {
        Properties props = System.getProperties();
        int defaultEncryptionType = this.getDefaultEncryptionType();
        if (defaultEncryptionType == EncryptionTypes.TLS.ordinal()) {
            props.put("mail.smtp.auth", String.valueOf(this.auth));
            props.put("mail.smtp.starttls.enable", "true");
        } else if (defaultEncryptionType == EncryptionTypes.SSL.ordinal()) {
            props.put("mail.smtp.host", this.mailHostSmtp);
            props.put("mail.smtp.socketFactory.port", this.mailPortSmtp);
            props.put("mail.smtp.socketFactory.class",
                    "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.debug", "true");
            props.put("mail.smtp.auth", String.valueOf(this.auth));
            props.put("mail.smtp.port", this.mailPortSmtp);
        } else {
            props.put("mail.smtp.host", this.mailHostSmtp);
            props.put("mail.smtp.auth", String.valueOf(this.auth));
        }
        return props;
    }

    private Properties getImapProperties() {
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imap");
        int defaultEncryptionType = EncryptionTypes.SSL.ordinal();
        if (defaultEncryptionType == EncryptionTypes.TLS.ordinal()) {
            props.put("mail.imap.auth.login.disable", String.valueOf(this.auth));
            props.put("mail.imap.starttls.enable", "true");
        } else if (defaultEncryptionType == EncryptionTypes.SSL.ordinal()) {
            props.put("mail.imap.host", this.mailHostImap);
            props.put("mail.imap.socketFactory.port", this.mailPortImap);
            props.put("mail.imap.socketFactory.class",
                    "javax.net.ssl.SSLSocketFactory");
            props.put("mail.imap.auth.login.disable", String.valueOf(this.auth));
            props.put("mail.imap.port", this.mailPortImap);
        } else {
            props.put("mail.imap.host", this.mailHostImap);
            props.put("mail.imap.auth.login.disable", String.valueOf(this.auth));
        }
        return props;
    }

    private Session getSession(Properties props) {
        Session session = null;
        if (this.getDefaultEncryptionType() == EncryptionTypes.TLS.ordinal()) {
            session = Session.getInstance(props);
        } else if (this.getDefaultEncryptionType() == EncryptionTypes.SSL
                .ordinal()) {
            session = Session.getInstance(props, new MyAuthenticator(
                    this.mailHostAccount, this.mailHostPassword));
        } else {
            session = Session.getDefaultInstance(props, null);
        }
        return session;
    }

    /**
     * @author zhangbo
     *
     */
    private class MyAuthenticator extends Authenticator {
        private String user;
        private String password;

        public MyAuthenticator(String user, String password) {
            this.user = user;
            this.password = password;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(this.user, this.password);
        }
    }

    /**
     * get default encryption type, for 465, SSL for 587, TLS
     * 
     * @return
     */
    private int getDefaultEncryptionType() {
        int rst = this.encryptionType;
        if (this.encryptionType == EncryptionTypes.Default.ordinal()) {
            if (this.mailPortSmtp == PORT_465 || this.mailPortImap == PORT_993) {
                rst = EncryptionTypes.SSL.ordinal();
            }
        }
        return rst;
    }

    /**
     * 运行shell
     * 
     * @param shStr
     *            需要执行的shell
     * @return
     * @throws IOException
     */
    public List<String> runShell(String shStr) throws IOException,
            InterruptedException {
        List<String> strList = new ArrayList<String>();
        Process process;
        process = Runtime.getRuntime().exec(
                new String[] { "/bin/sh", "-c", shStr }, null, null);
        InputStreamReader ir = new InputStreamReader(process.getInputStream());
        LineNumberReader input = new LineNumberReader(ir);
        String line;
        process.waitFor();
        while ((line = input.readLine()) != null) {
            strList.add(line);
        }
        return strList;
    }

    public String getMailHostSmtp() {
        return mailHostSmtp;
    }

    public void setMailHostSmtp(String mailHostSmtp) {
        this.mailHostSmtp = mailHostSmtp;
    }

    public int getMailPortSmtp() {
        return mailPortSmtp;
    }

    public void setMailPortSmtp(int mailPortSmtp) {
        this.mailPortSmtp = mailPortSmtp;
    }

    public String getMailHostImap() {
        return mailHostImap;
    }

    public void setMailHostImap(String mailHostImap) {
        this.mailHostImap = mailHostImap;
    }

    public int getMailPortImap() {
        return mailPortImap;
    }

    public void setMailPortImap(int mailPortImap) {
        this.mailPortImap = mailPortImap;
    }

    public int getEncryptionType() {
        return encryptionType;
    }

    public void setEncryptionType(int encryptionType) {
        this.encryptionType = encryptionType;
    }

    public boolean isAuth() {
        return auth;
    }

    public void setAuth(boolean auth) {
        this.auth = auth;
    }

    public String getMailHostAccount() {
        return mailHostAccount;
    }

    public void setMailHostAccount(String mailHostAccount) {
        this.mailHostAccount = mailHostAccount;
    }

    public String getMailHostPassword() {
        return mailHostPassword;
    }

    public void setMailHostPassword(String mailHostPassword) {
        this.mailHostPassword = mailHostPassword;
    }

    public String getConfigFilesMailRecipients() {
        return configFilesMailRecipients;
    }

    public void setConfigFilesMailRecipients(String configFilesMailRecipients) {
        this.configFilesMailRecipients = configFilesMailRecipients;
    }

    public String getCnnServersMailRecipients() {
        return cnnServersMailRecipients;
    }

    public void setCnnServersMailRecipients(String cnnServersMailRecipients) {
        this.cnnServersMailRecipients = cnnServersMailRecipients;
    }

    public String getSearchServersMailRecipients() {
        return searchServersMailRecipients;
    }

    public void setSearchServersMailRecipients(
            String searchServersMailRecipients) {
        this.searchServersMailRecipients = searchServersMailRecipients;
    }

    public String getNlpServersMailRecipients() {
        return nlpServersMailRecipients;
    }

    public void setNlpServersMailRecipients(String nlpServersMailRecipients) {
        this.nlpServersMailRecipients = nlpServersMailRecipients;
    }

    public String getConfigFilesMailSenders() {
        return configFilesMailSenders;
    }

    public void setConfigFilesMailSenders(String configFilesMailSenders) {
        this.configFilesMailSenders = configFilesMailSenders;
    }

    /**
     * @author zhangbo
     *
     */
    public enum EncryptionTypes {
        Default, TLS, SSL;
    }

    /**
     * @author zhangbo
     *
     */
    public enum EmailType {
        CONFIG("config");

        private final String value;

        public String getValue() {
            return value;
        }

        EmailType(String value) {
            this.value = value;
        }
    }

    public static void main(String[] args) {
        // test tar start
        // UgcCommonStrategy.tarDecompress(getBytes("test.tar"));
        // test tar end

        // EmailUtil eu = new EmailUtil();
        // eu.setMailHostSmtp("smtp.exmail.qq.com");
        // eu.setMailPortSmtp(PORT_465);
        // eu.setEncryptionType(EncryptionTypes.SSL.ordinal());
        // eu.setMailHostImap("imap.exmail.qq.com");
        // eu.setMailPortImap(PORT_993);
        // eu.setAuth(true);
        // eu.setMailHostAccount("wenba_scheduler@wenba100.com");
        // eu.setMailHostPassword("Wenbascheduler01");
        // EmailParam emailParam = new EmailParam();
        // emailParam.setSchedulerId("wenba_scheduler_01");
        // emailParam
        // .setReceiverAddress("bo.zhang@wenba100.com;zb_ramble@163.com;zb_ramble@126.com");
        // emailParam.setSub("hello");
        // emailParam.setMsg("hello world!");
        // try {
        // eu.sendEmail(emailParam);
        // } catch (Exception e) {
        // e.printStackTrace();
        // }

        // System.out.println("EncryptionTypes.SSL.ordinal(): "
        // + EncryptionTypes.SSL.ordinal());

        // EmailUtil eu = new EmailUtil();
        // eu.setMailHostImap("imap.exmail.qq.com");
        // eu.setMailPortImap(PORT_993);
        // eu.setEncryptionType(EncryptionTypes.SSL.ordinal());
        // eu.setAuth(true);
        // eu.setMailHostAccount("wenba_scheduler@wenba100.com");
        // eu.setMailHostPassword("Wenbascheduler01");
        // EmailParam emailParam = new EmailParam();
        // eu.receiveConfigEmail(emailParam);

        // try {
        // List<String> shellResultList = eu.runShell(args[0]);
        // for (String shellResult : shellResultList) {
        // System.out.println(shellResult);
        // }
        // } catch (Exception e) {
        // e.printStackTrace();
        // }

        // try {
        // Calendar startTime = Calendar.getInstance();
        // Calendar stopTime = Calendar.getInstance();
        // // SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        // SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        //
        // Date startDate = df.parse("20150501" + "000000");
        // // Date startDate = df.parse("20150501000000");
        // startTime.setTime(startDate);
        //
        // Date stopDate = df.parse("20150501" + "010000");
        // // Date stopDate = df.parse("20150501010000");
        // stopTime.setTime(stopDate);
        //
        // while (startDate.compareTo(stopDate) <= 0) {
        // for (int i = 0; i < 10; ++i) {
        // System.out.println(df.format(startDate)
        // + String.format("%06d", i));
        // }
        // // startTime.add(Calendar.DATE, 1);
        // startTime.add(Calendar.SECOND, 1);
        // startDate = startTime.getTime();
        // }
        //
        // // System.out.println(String.format("%06d", 0));
        // } catch (Exception e) {
        //
        // }
    }

    /**
     * 获得指定文件的byte数组
     */
    public static byte[] getBytes(String filePath) {
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1073741820);
            byte[] b = new byte[1073741820];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }
}
