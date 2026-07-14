package service;
import model.Message;
// faz 2
import repository.MessageRepository;
import repository.ReportRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MessageService {
    private static final int haveMessagesMax = 500;

    private final List<Message> messages;
    private final GroupService groupService;
    private final MessageRepository messageRepository;
    private final ReportRepository reportRepository;

    private static final int maxSpamM = 5;
    private static final long timeSpamM = 1000;

    private final List<String> senderID;
    private final List<Long> recentTimeM;

    public MessageService(GroupService groupService) {
        this.groupService = groupService;
        this.messageRepository = new MessageRepository();
        this.reportRepository = new ReportRepository();

        this.messages = new ArrayList<>(messageRepository.loadAll());

        this.senderID = new ArrayList<>();
        this.recentTimeM = new ArrayList<>();
        List<String> reportedMessageIds = reportRepository.loadAllMessageIds();
        loadReportedMessages(reportedMessageIds);
        System.out.println("load" + messages.size() + "messages from messages.txt.");
        System.out.println("load"+ getReportedMessagesInternal().size() +"reports from reports.txt.");
    }
    public synchronized Message sendMessage(String chatId, String senderId, String content){
        if (isBlank(chatId) || isBlank(senderId) || isBlank(content)) {
            return null;
        }

        if (content.length() > haveMessagesMax){
            return null;
        }
        if (isSpam(senderId)) {
            return null;
        }
        Message message = new Message(UUID.randomUUID().toString(), chatId, senderId, content.trim());
        messages.add(message);
        addRecentMessage(senderId);
        persistMessages();
        return message;
    }
    public synchronized List<Message> getMessagesByChatId(String chatId){
        List<Message> result = new ArrayList<>();
        for (Message message : messages){
            if (message.getChatId().equalsIgnoreCase(chatId)){
                result.add(message);
               }
        }
        return result;
     }
    public synchronized Message getMessageById(String messageId){
        if (messageId == null){
            return null;
        }
        for (Message message : messages){
            if (message.getId().equalsIgnoreCase(messageId)){
                return message;
              }
        }
        return null;
         }
    public synchronized boolean reportMessage(String messageId){
        Message message = getMessageById(messageId);
        if (message == null) {
            return false;
        }
        if (message.isReported()){
            return false;
        }
        message.setReported(true);
        groupService.reportMessage(message);
        persistMessages();
        persistReports();

        return true;
    }
    // faz 2 changes
    private void loadReportedMessages(List<String> reportedMessageIds) {
        boolean changed = false;
        for (Message message : messages) {
            boolean reportedInMessagesFile = message.isReported();
            boolean reportedInReportsFile =
                    containsId(reportedMessageIds, message.getId());
            if (reportedInMessagesFile || reportedInReportsFile) {
                if (!message.isReported()) {
                    message.setReported(true);
                    changed = true;
                }
                groupService.reportMessage(message);
              }
        }

        if (changed) {
            persistMessages();
           }
        persistReports();
      }
    // faz 2 changes
    private boolean containsId(List<String> ids, String messageId) {
        for (String id : ids){
            if (id.equalsIgnoreCase(messageId)){
                return true;
            }
           }
        return false;
        }

    // faz 2 changes
    private List<Message> getReportedMessagesInternal(){
        List<Message> result = new ArrayList<>();
        for (Message message : messages) {
            if (message.isReported()) {
                result.add(message);
             }
        }
        return result;
    }
    private void persistMessages(){
        messageRepository.saveAll(messages);
    }
    // faz 2 changes
    private void persistReports(){
        reportRepository.saveAll(getReportedMessagesInternal());
    }
    private boolean isSpam(String senderId) {
        long now = System.currentTimeMillis();
        int count = 0;
        int i = 0;

        while (i < senderID.size()) {
            long oldTime = recentTimeM.get(i);

            if (now - oldTime > timeSpamM) {
                senderID.remove(i);
                recentTimeM.remove(i);
            } else {
                if (senderID.get(i).equalsIgnoreCase(senderId)) {
                    count++;
                }
                i++;
            }
        }
        return count >= maxSpamM;
    }
    private void addRecentMessage(String senderId) {
        senderID.add(senderId);
        recentTimeM.add(System.currentTimeMillis());
    }

        private boolean isBlank(String value){
        return value == null || value.isBlank();
    }

}