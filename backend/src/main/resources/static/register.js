document.getElementById("registerForm").addEventListener("submit", function (e) {
    e.preventDefault();

    const fullName = document.getElementById("registerFullName").value.trim();
    const email = document.getElementById("registerEmail").value.trim();
    const password = document.getElementById("registerPassword").value;
    const profileImage = document.getElementById("registerProfileImage").files[0];

    if (!profileImage) {
        alert("Please select a profile image.");
        return;
    }

    const formData = new FormData();
    formData.append("displayName", fullName);
    formData.append("email", email);
    formData.append("password", password);
    formData.append("profileImage", profileImage);

    fetch("/api/auth/register", {
        method: "POST",
        body: formData
        // ❌ DO NOT set Content-Type manually
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("Registration failed");
        }
        return response.text();
    })
    .then(message => {
        alert(message);

        // Clear form
        document.getElementById("registerForm").reset();

        // Switch to login panel
        document.getElementById("container")
            .classList.remove("right-panel-active");
    })
    .catch(error => {
        console.error(error);
        alert("❌ Registration failed.");
    });
});
