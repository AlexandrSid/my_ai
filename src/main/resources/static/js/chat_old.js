document.addEventListener("DOMContentLoaded", function() {
    const sendButton = document.getElementById("send-button");
    const chatInput = document.getElementById("chat-input");
    const messagesContainer = document.getElementById("messages");
    const form = document.querySelector(".input-area");

    sendButton.addEventListener("click", function() {
        const prompt = chatInput.value.trim();
        if (!prompt) return;

        // Находим action формы (он уже содержит chatId)
        const url = form.getAttribute("action");

        // Делаем POST-запрос
        fetch(url, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: new URLSearchParams({
                prompt: prompt
            })
        })
            .then(response => {
                if (response.redirected) {
                    // Если сервер вернул redirect (обычно после POST),
                    // то перенаправляем браузер
                    window.location.href = response.url;
                } else {
                    return response.text();
                }
            })
            .then(data => {
                if (data) {
                    // Если сервер вернул HTML — заменим страницу
                    document.open();
                    document.write(data);
                    document.close();
                }
            })
            .catch(error => {
                console.error("Ошибка при отправке:", error);
            });

        // Добавляем сообщение пользователя сразу в чат
        const userDiv = document.createElement("div");
        userDiv.className = "message user";
        userDiv.innerHTML = `<img src="/images/user.png" alt="User"><div class="bubble">${prompt}</div>`;
        messagesContainer.appendChild(userDiv);

        // Очищаем поле ввода
        chatInput.value = "";
    });
});
