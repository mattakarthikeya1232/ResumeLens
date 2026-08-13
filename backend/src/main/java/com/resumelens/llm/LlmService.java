package com.resumelens.llm;

import com.resumelens.dto.Explanation;
import com.resumelens.model.ClassificationOutput;

public interface LlmService {
    boolean isAvailable();
    String modelName();
    Explanation explain(ClassificationOutput analysis);
}
