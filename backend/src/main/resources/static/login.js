document.getElementById("loginForm").addEventListener("submit", function(e) {
    e.preventDefault();

    const email = document.getElementById("loginEmail").value.trim();
    const password = document.getElementById("loginPassword").value;

    fetch("/api/auth/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        credentials: "include",
        body: JSON.stringify({
            email: email,
            password: password
        })
    })
    .then(res => {
        if (!res.ok) throw new Error();
        
        // ✅ Login successful → redirect
        window.location.href = "/index.html";
    })
    .catch(() => {
        alert("❌ Invalid email or password");
    });
});
