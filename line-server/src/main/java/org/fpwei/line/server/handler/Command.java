package org.fpwei.line.server.handler;

import com.linecorp.bot.model.message.Message;

public interface Command {
    Message execute(String parameters);
}
