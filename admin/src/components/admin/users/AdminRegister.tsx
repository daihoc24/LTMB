import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import styles from "../../login/register.module.css";
import { useDispatch, useSelector } from "react-redux";
import { RootState } from "../../reduxStore/Store";

function AdminRegister() {
  const [fullName, setFullName] = useState<string>("");
  const [email, setEmail] = useState<string>("");
  const [phoneNumber, setPhoneNumber] = useState<string>("");
  const [username, setUsername] = useState<string>("");
  const [password, setPassword] = useState<string>("");
  const [confirmPassword, setConfirmPassword] = useState<string>("");
  const [error, setError] = useState<string>("");
  const [success, setSuccess] = useState<string>("");
  const [loading, setLoading] = useState<boolean>(false);

  const navigate = useNavigate();
  const currentUser = useSelector((state: RootState) => state.user.currentUser);
  const dispatch = useDispatch();

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    
    if (password !== confirmPassword) {
      setError("Mật khẩu không khớp");
      return;
    }
    
    setLoading(true);
    try {
      // Gọi API tạo admin với đúng endpoint backend
      const response = await axios.post(
        "http://localhost:8080/chat-application/v1/users/createAdmin",
        {
          fullName,
          email,
          phoneNumber,
          username,
          password,
          confirmPassword,
        },
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("authToken")}`,
          },
        }
      );
      
      // Kiểm tra response thành công
      if (response.data && response.data.result) {
        // Gửi email kích hoạt tài khoản
        try {
          await axios.post(
            "http://localhost:8080/chat-application/v1/users/requireActivateAccount",
            null,
            {
              params: { emailorUsername: email },
              headers: {
                Authorization: `Bearer ${localStorage.getItem("authToken")}`,
              },
            }
          );
          setSuccess("Đăng ký thành công! Email kích hoạt đã được gửi.");
        } catch (emailError) {
          console.error("Gửi mail kích hoạt không thành công!", emailError);
          setSuccess("Đăng ký thành công! Nhưng không thể gửi email kích hoạt.");
        }
        
        setTimeout(() => {
          resetForm();
        }, 3000);
      } else {
        setError(response.data?.message || "Đăng ký không thành công. Vui lòng thử lại sau.");
      }
    } catch (err) {
      if (axios.isAxiosError(err)) {
        // Xử lý lỗi từ backend
        const errorData = err.response?.data;
        let errorMsg = "Đăng ký không thành công. Vui lòng thử lại sau.";
        
        if (errorData) {
          // Backend trả về message trong ApiResponse
          if (errorData.message) {
            errorMsg = errorData.message;
          } else if (errorData.title) {
            errorMsg = errorData.title;
          } else if (typeof errorData === "string") {
            errorMsg = errorData;
          }
        }
        
        setError(errorMsg);
      } else {
        setError("Đã xảy ra lỗi. Vui lòng thử lại sau.");
      }
      console.error("Registration failed:", err);
    } finally {
      setLoading(false);
    }
  };
  const resetForm = () => {
    setFullName("");
    setEmail("");
    setPhoneNumber("");
    setUsername("");
    setPassword("");
    setConfirmPassword("");
  };
  return (
    <div
      className={styles.bigContainer}
      style={{ margin: "0", padding: "0", transform: "translateX(0px)" }}
    >
      <div className={styles.authContainer} style={{ margin: "0" }}>
        <form onSubmit={handleRegister}>
          <div className={styles.formGroup}>
            <label>Họ và Tên</label>
            <input
              type="text"
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              required
            />
          </div>
          <div className={styles.formGroup}>
            <label>Email</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>
          <div className={styles.formGroup}>
            <label>Số Điện Thoại</label>
            <input
              type="text"
              value={phoneNumber}
              onChange={(e) => setPhoneNumber(e.target.value)}
              required
            />
          </div>
          <div className={styles.formGroup}>
            <label>Tên Đăng Nhập</label>
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
          </div>
          <div className={styles.formGroup}>
            <label>Mật Khẩu</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>
          <div className={styles.formGroup}>
            <label>Xác Nhận Mật Khẩu</label>
            <input
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
            />
          </div>
          {error && (
            <p
              style={{
                color: "red",
                fontWeight: 600,
                fontStyle: "italic",
                textAlign: "center",
              }}
            >
              {error}
            </p>
          )}
          {success && (
            <p
              style={{
                color: "green",
                fontWeight: 600,
                fontStyle: "italic",
                textAlign: "center",
              }}
            >
              {success}
            </p>
          )}
          <button type="submit" className={styles.btn} disabled={loading}>
            {loading ? "Đang Xử Lý..." : "Đăng Ký"}
          </button>
          {loading && <div className={styles.loader}></div>}
        </form>
      </div>
    </div>
  );
}

export default AdminRegister;
