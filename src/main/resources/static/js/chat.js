document.addEventListener("DOMContentLoaded", function() {
    const sendButton = document.getElementById("send-button");
    const chatInput = document.getElementById("chat-input");
    const messagesContainer = document.getElementById("messages");

    sendButton.addEventListener("click", function() {
        const prompt = chatInput.value;
        if (!prompt) return;
        chatInput.value = "";

        // Добавляем сообщение пользователя в чат
        const userDiv = document.createElement("div");
        userDiv.className = "message user";
        userDiv.innerHTML = `<img src="/images/user.png" alt="User"><div class="bubble">${prompt}</div>`;
        messagesContainer.appendChild(userDiv);

        const pathParts = window.location.pathname.split("/");
        const chatId = pathParts[pathParts.length - 1];
        const url = `/chat-stream/${chatId}?userPrompt=${encodeURIComponent(prompt)}`;

        const eventSource = new EventSource(url);
        let fullText = "";

        // Создаем блок для ответа AI
        const aiDiv = document.createElement("div");
        aiDiv.className = "message mentor";
        // Добавляем изображение ассистента
        aiDiv.innerHTML = `<img src="/images/mentor.png" alt="Mentor">`;
        // Создаем элемент для содержимого, куда будем вставлять ответ
        const aiBubble = document.createElement("div");
        aiBubble.className = "bubble";
        aiDiv.appendChild(aiBubble);
        messagesContainer.appendChild(aiDiv);

        eventSource.onmessage = function(event) {
            const data = JSON.parse(event.data);
            let token = data.text;
            console.log(token);
            fullText += token;
            // Преобразуем Markdown в HTML (при условии, что marked.js подключен)
            aiBubble.innerHTML = marked.parse(fullText);
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        };

        eventSource.onerror = function(e) {
            console.error("Ошибка SSE:", e);
            eventSource.close();
        };
    });
});
document.addEventListener('DOMContentLoaded', function() {
    const messageBubbles = document.querySelectorAll('.message.mentor .bubble');

    messageBubbles.forEach(bubble => {
        // Получаем текст из элемента.
        // Примечание: Thymeleaf с th:text уже поместил сюда чистый текст.
        // С th:utext это будет уже HTML, поэтому этот шаг нужен только если вы
        // НЕ изменили HTML на th:utext.
        const markdownText = bubble.textContent;

        // Преобразуем Markdown в HTML.
        const htmlContent = marked.parse(markdownText);

        // Вставляем HTML обратно в элемент.
        bubble.innerHTML = htmlContent;
    });
});
