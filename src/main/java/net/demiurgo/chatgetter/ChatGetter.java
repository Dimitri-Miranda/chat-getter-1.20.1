package net.demiurgo.chatgetter;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ChatGetter implements ClientModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger("ChatGetter");
	private static final String PYTHON_CHAT_URL = "http://127.0.0.1:5000/getChat";
	private static final String AI_RESPONSE_TXT = "e:/aitxt.txt";
	private ArrayList<String> chatDuplicate = new ArrayList<>();
	private ArrayList<String> chatAiDuplicate = new ArrayList<>();
	private String ultimaLinha;

    @Override
	public void onInitializeClient() {
		if (MinecraftClient.getInstance().player != null){
			System.out.println("Não é nulo");
			try {
				File aiTxt = new File(AI_RESPONSE_TXT);
				Scanner scanner = new Scanner(aiTxt);
				while (scanner.hasNextLine()) {
					ultimaLinha = scanner.nextLine();
				}
				if (!chatAiDuplicate.contains(ultimaLinha)){
					chatAiDuplicate.add(ultimaLinha);
					writeAiMessage(ultimaLinha);

				}
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		// Registrar callback para mensagens de chat
		ClientReceiveMessageEvents.CHAT.register((message, signed_message, sender, params, timestamp) -> {
			LOGGER.info("CHAT Message from {}: {} with params {}", sender, message, params);
			if (!chatDuplicate.contains(message)){
				chatDuplicate.add(message.getString());
                try {
                    postChatMessage(message.getString());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

		});


    }

	//envia a mensagem que o player digitou no chat
	private void postChatMessage(String message) throws IOException {
		URL pythonUrl = new URL(PYTHON_CHAT_URL);
		HttpURLConnection connection = (HttpURLConnection) pythonUrl.openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);

		String jsonMessage = "{\"message\": \""+message+"\"}";

		try (OutputStream os = connection.getOutputStream()) {
			byte[] input = jsonMessage.getBytes(StandardCharsets.UTF_8);
			os.write(input, 0, input.length);
		}

		int responseCode = connection.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_OK) {
			System.out.println("Mensagem enviada com sucesso!");
		} else {
			System.out.println("Erro na requisição: " + responseCode);
		}

	}
	private void writeAiMessage(String aiMessage){
		System.out.println("Voce chegou na parte");
		MinecraftClient.getInstance().player.sendMessage(Text.of(aiMessage));
		//MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of(aiMessage));
	}
}
