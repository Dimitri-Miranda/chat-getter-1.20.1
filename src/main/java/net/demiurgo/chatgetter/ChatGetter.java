package net.demiurgo.chatgetter;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatGetter implements ClientModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger("ChatGetter");
	private static final String FILE_PATH = "e:/chat_messages.txt";

	@Override
	public void onInitializeClient() {
		// Registrar callback para mensagens de chat
		ClientReceiveMessageEvents.CHAT.register((message, signed_message, sender, params, timestamp) -> {
			LOGGER.info("CHAT Message from {}: {} with params {}", sender, message, params);
			saveMessageToFile(String.valueOf(sender), message.getString());
		});
	}

	private void saveMessageToFile(String sender, String message) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
			String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
			writer.write("[" + timestamp + "] " + sender + ": " + message);
			writer.newLine();
		} catch (IOException e) {
			LOGGER.error("Failed to save message to file", e);
		}
	}
}
