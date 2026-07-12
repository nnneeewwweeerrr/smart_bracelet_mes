package com.mes.ai;

import java.util.List;

public interface AiModelAdapter {

    String generate(String prompt);

    String generateWithHistory(String prompt, List<ChatMessage> history);
}
