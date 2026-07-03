import React, { useEffect, useState } from "react";
import axios from "axios";
import Swal from "sweetalert2";
import styles from "../blogDetail/BlogDetail.module.css";
import { Link, useNavigate, useParams } from "react-router-dom";
import { CKEditor } from "@ckeditor/ckeditor5-react";
import ClassicEditor from "@ckeditor/ckeditor5-build-classic";
import { FaArrowCircleLeft, FaPlus } from "react-icons/fa";
import { useDispatch, useSelector } from "react-redux";
import { RootState } from "../../reduxStore/Store";

const BlogForm: React.FC = () => {
  const { blogId } = useParams<{ blogId?: string }>();
  const [title, setTitle] = useState("");
  const [authId, setAuthId] = useState("");
  const [image, setImage] = useState<File | null>(null);
  const [imageUrl, setImageUrl] = useState<string | null>(null);
  const [shortDescription, setShortDescription] = useState("");
  const [content, setContent] = useState("");
  const [contentError, setContentError] = useState("");
  const [shortDescError, setShortDescError] = useState("");
  const [editorReady, setEditorReady] = useState(false);

  const navigate = useNavigate();
  const currentUser = useSelector((state: RootState) => state.user.currentUser);
  const dispatch = useDispatch();

  useEffect(() => {
    if (
      currentUser == undefined ||
      currentUser == null ||
      (currentUser && currentUser.role !== 0 && currentUser.role !== "admin")
    ) {
      navigate("/unauthorized");
    }
  }, [currentUser, navigate]);

  useEffect(() => {
    const fetchAndConvertImage = async () => {
      try {
        if (imageUrl) {
          const response = await fetch(imageUrl);
          if (!response.ok) throw new Error("Network response was not ok");

          // Chuyển đổi dữ liệu thành Blob
          const blob = await response.blob();

          // Tạo đối tượng File từ Blob
          const file = new File([blob], "image.jpg", { type: blob.type });
          setImage(file);
        }
      } catch (error) {
        console.error("Error fetching or converting image:", error);
      }
    };

    fetchAndConvertImage();
  }, [imageUrl]);

  useEffect(() => {
    if (blogId) {
      const fetchBlogDetails = async () => {
        try {
          const response = await axios.get(
            `http://localhost:8080/chat-application/v1/post/blog/${blogId}`,
            {
              headers: {
                Authorization: `Bearer ${localStorage.getItem("authToken")}`,
              },
            },
          );

          // Backend trả về ApiResponse format
          const post = response.data?.result || response.data;

          if (post) {
            // Parse caption: format là "title\n\nshortDescription\n\ncontent"
            const captionParts = post.caption?.split("\n\n") || [];
            if (captionParts.length >= 3) {
              setTitle(captionParts[0] || "");
              setShortDescription(captionParts[1] || "");
              setContent(captionParts.slice(2).join("\n\n") || "");
            } else if (captionParts.length === 2) {
              setTitle(captionParts[0] || "");
              setShortDescription(captionParts[1] || "");
              setContent("");
            } else if (captionParts.length === 1) {
              setTitle(captionParts[0] || "");
              setShortDescription("");
              setContent("");
            }

            // Set authId từ user object
            if (post.user?.id) {
              setAuthId(post.user.id);
            }

            // Đánh dấu editor sẵn sàng sau khi data đã load
            setEditorReady(true);

            // Note: Image sẽ được lấy từ Media entity riêng (cần API khác)
            // Tạm thời để null, có thể thêm API call để lấy media sau
            // setCategoryId(post.categoryId || ""); // Category không có trong Post entity
          }
        } catch (error) {
          console.error("Failed to fetch blog details", error);
          Swal.fire({
            icon: "error",
            title: "Lỗi",
            text: "Không thể tải chi tiết bài viết",
          });
        }
      };

      fetchBlogDetails();
    }
  }, [blogId]);


  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setImage(e.target.files[0]);
      setImageUrl(URL.createObjectURL(e.target.files[0])); // Create a URL for the selected image
    }
  };

  // submit form create
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (content.length === 0) {
      setContentError("Nội dung không được để trống.");
    } else {
      setContentError("");
    }

    if (shortDescription.length === 0) {
      setShortDescError("Mô tả ngắn không được để trống.");
    } else {
      setShortDescError("");
    }

    if (content.length > 0 && shortDescription.length > 0) {
      console.log("auth : ", authId);
      try {
        let finalImageUrl = "";
        if (image) {
          // Lấy presigned URL từ backend
          const presignedResponse = await axios.get(
            "http://localhost:8080/chat-application/v1/post/generatePresignedUrl",
            {
              headers: {
                Authorization: `Bearer ${localStorage.getItem("authToken")}`,
              },
            }
          );

          // Backend trả về ApiResponse format
          const presignedUrl = presignedResponse.data?.result || presignedResponse.data;

          // Note: Backend hiện tại chỉ trả về Cloudinary URL template
          // Cần upload image lên Cloudinary hoặc sử dụng MediaController
          // Tạm thời, có thể upload trực tiếp lên Cloudinary hoặc dùng MediaController
          // Để đơn giản, tạm thời lưu imageUrl rỗng hoặc dùng CloudinaryController

          // TODO: Implement image upload using CloudinaryController hoặc MediaController
          // Ví dụ: upload lên CloudinaryController
          try {
            const formData = new FormData();
            formData.append("fileUpload", image);
            formData.append("userId", currentUser?.id || authId || "");
            formData.append("postId", "0"); // Tạm thời, sẽ update sau khi tạo post

            const uploadResponse = await axios.post(
              "http://localhost:8080/chat-application/v1/cloudinary/one",
              formData,
              {
                headers: {
                  Authorization: `Bearer ${localStorage.getItem("authToken")}`,
                  "Content-Type": "multipart/form-data",
                },
              }
            );

            if (uploadResponse.data?.result) {
              finalImageUrl = uploadResponse.data.result;
            }
          } catch (uploadError) {
            console.error("Failed to upload image:", uploadError);
            // Nếu upload thất bại, vẫn tiếp tục nhưng không có image
          }
        } else if (imageUrl) {
          finalImageUrl = imageUrl; // Use existing image URL if no new image is uploaded
        }

        const userId = currentUser?.id || authId || "";

        if (blogId) {
          // Update existing blog
          const blogUpdate = {
            id: blogId,
            authId: authId || userId,
            title,
            image: finalImageUrl,
            shortDescription,
            content,
            categoryId: "",
            status: 1,
          };

          try {
            const response = await axios.post(
              "http://localhost:8080/chat-application/v1/post/updateBlog",
              blogUpdate,
              {
                headers: {
                  "Content-Type": "application/json",
                  Authorization: `Bearer ${localStorage.getItem("authToken")}`,
                },
              },
            );

            Swal.fire({
              icon: "success",
              title: "Đã lưu thành công",
              toast: true,
              position: "center",
              showConfirmButton: false,
              timer: 2000,
              timerProgressBar: true,
              didOpen: (toast) => {
                toast.onmouseenter = Swal.stopTimer;
                toast.onmouseleave = Swal.resumeTimer;
              },
            });
            setTimeout(() => {
              navigate(`/admin/blogs`);
            }, 600);
          } catch (error: any) {
            console.error("blogUpdateError:", error);
            Swal.fire({
              icon: "error",
              title: "Lỗi",
              text: error.response?.data?.message || "Không thể cập nhật bài viết",
            });
          }
        } else {
          // Create new blog
          const blogData = {
            authId: userId,
            title,
            image: finalImageUrl,
            shortDescription,
            content,
            categoryId: "",
            status: 1,
            numLike: 0,
            createAt: new Date().toISOString(),
          };

          try {
            const response = await axios.post(
              "http://localhost:8080/chat-application/v1/post/createBlog",
              blogData,
              {
                headers: {
                  "Content-Type": "application/json",
                  Authorization: `Bearer ${localStorage.getItem("authToken")}`,
                },
              },
            );

            Swal.fire({
              icon: "success",
              title: "Tạo bài viết thành công",
              toast: true,
              position: "center",
              showConfirmButton: false,
              timer: 2000,
              timerProgressBar: true,
              didOpen: (toast) => {
                toast.onmouseenter = Swal.stopTimer;
                toast.onmouseleave = Swal.resumeTimer;
              },
            });
            setTimeout(() => {
              navigate(`/admin/blogs`);
            }, 600);
          } catch (error: any) {
            console.error("blogCreateError:", error);
            Swal.fire({
              icon: "error",
              title: "Lỗi",
              text: error.response?.data?.message || "Không thể tạo bài viết",
            });
          }
        }
      } catch (error) {
        Swal.fire({
          icon: "warning",
          title: "Lưu thất bại!",
          toast: true,
          position: "top-end",
          showConfirmButton: false,
          timer: 5000,
          timerProgressBar: true,
          didOpen: (toast) => {
            toast.onmouseenter = Swal.stopTimer;
            toast.onmouseleave = Swal.resumeTimer;
          },
        });
      }
    }
  };
  return (
    <div className={styles.container}>
      <Link
        to={"/admin/blogs"}
        className={styles.addIcon}
        style={{
          float: "left",
          fontWeight: "bold",
          fontSize: "25px",
          border: "none",
          margin: "0",
          padding: "0",
        }}
        title="Quay lại"
      >
        <FaArrowCircleLeft />
      </Link>
      <h2>Chi tiết bài viết</h2>

      <form onSubmit={handleSubmit}>
        <div className={styles.formGroup}>
          <label htmlFor="title">Tiêu đề</label>
          <input
            type="text"
            className={styles.formControl}
            id="title"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            required
          />
        </div>
        <div className={styles.formGroup}>
          <label htmlFor="image">Hình ảnh (Tùy chọn)</label>
          <input
            type="file"
            className={styles.formControl}
            id="image"
            style={{ background: "white" }}
            accept=".jpg, .jpeg, .png, .gif, .svg"
            onChange={handleImageChange}
          />
          {(imageUrl || image) && (
            <div>
              <img
                src={imageUrl || URL.createObjectURL(image!)}
                alt="Selected"
                style={{
                  maxWidth: "100%",
                  maxHeight: "200px",
                  marginTop: "10px",
                }}
              />
            </div>
          )}
        </div>
        <div className={styles.formGroup}>
          <label htmlFor="shortDescription">Mô tả ngắn</label>
          <textarea
            style={{ height: "100px" }}
            className={styles.formControl}
            id="shortDescription"
            rows={8}
            value={shortDescription}
            onChange={(e) => setShortDescription(e.target.value)}
            required
          />
          {shortDescError && (
            <label className={styles.errorInput}>{shortDescError}</label>
          )}
        </div>
        <div className={styles.formGroup}>
          <label htmlFor="content">Nội dung</label>
          {/* <textarea
            style={{ height: "100px" }}
            className={styles.formControl}
            id="content"
            rows={8}
            value={content}
            onChange={(e) => setContent(e.target.value)}
            required
          /> */}
          {editorReady || !blogId ? (
            <CKEditor
              key={blogId || "new"}
              editor={ClassicEditor as any}
              data={content}
              onReady={(editor) => {
                setEditorReady(true);
              }}
              onChange={(event, editor) => {
                const data = editor.getData();
                setContent(data);
              }}
              onError={(error, { willEditorRestart }) => {
                if (willEditorRestart) {
                  setEditorReady(false);
                }
              }}
              config={{
                ckfinder: {
                  uploadUrl: "https://your-upload-url",
                },
                toolbar: [
                  "heading",
                  "|",
                  "bold",
                  "italic",
                  "underline",
                  "strikethrough",
                  "alignment",
                  "|",
                  "link",
                  "bulletedList",
                  "numberedList",
                  "blockQuote",
                  "imageUpload",
                  "|",
                  "undo",
                  "redo",
                ],
                image: {
                  toolbar: [
                    "imageTextAlternative",
                    "imageStyle:full",
                    "imageStyle:side",
                  ],
                },
              } as any}
            />
          ) : (
            <div style={{ padding: "20px", textAlign: "center", color: "#6b7280" }}>
              Đang tải editor...
            </div>
          )}
          {contentError && (
            <label className={styles.errorInput}>{contentError}</label>
          )}
        </div>
        <div className={"${styles.formGroup} ${styles.mt3}"}>
          <button type="submit" className={styles.buttonSubmit}>
            Lưu
          </button>
        </div>
      </form>
    </div>
  );
};

export default BlogForm;
