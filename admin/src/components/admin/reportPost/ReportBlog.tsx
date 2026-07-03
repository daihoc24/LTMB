import React, { useState, useEffect } from "react";
import axios from "axios";
import DataTable from "react-data-table-component";
import { format, parseISO } from "date-fns"; // hỗ trợ định dạng ngày tháng theo mẫu
import { FaAccessibleIcon, FaEdit, FaKeyboard, FaPlus, FaTrash } from "react-icons/fa";
import { Link, useNavigate } from "react-router-dom";
import styles from "../blog/Blog.module.css";
import Swal from "sweetalert2";
import { useDispatch, useSelector } from "react-redux";
import { RootState } from "../../reduxStore/Store";
import ReactPaginate from "react-paginate";

interface Blog {
  reportedPostId: number;
  reporterId: string;
  reporterUsername: string;
  context: string;
  caption: string;
  createdAt: string;
  // Thêm các trường khác nếu cần thiết
}

const ReportBlog: React.FC = () => {
  const navigate = useNavigate();
  const [searchData, setSearchData] = useState<Blog[]>();
  const currentUser = useSelector((state: RootState) => state.user.currentUser);
  const [blogs, setBlogs] = useState<Blog[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string>("");
  const [currentPage, setCurrentPage] = useState<number>(0);
  const [itemsPerPage, setItemsPerPage] = useState<number>(10);

  useEffect(() => {
    const fetchBlogs = async () => {
      try {
        const response = await axios.post(
          "http://localhost:8080/chat-application/v1/reportPost/getReportPort"
        );
        setBlogs(response.data);
        setSearchData(response.data);
        setLoading(false);
      } catch (error) {
        setError("Lỗi khi tải danh sách bài viết.");
        setLoading(false);
      }
    };

    fetchBlogs();
  }, []);

  const handleSearch = (event: React.ChangeEvent<HTMLInputElement>) => {
    const newData = blogs.filter((row) => {
      return row.caption
        .toLowerCase()
        .includes(event.target.value.toLowerCase());
    });
    setSearchData(newData);
    setCurrentPage(0); // Reset về trang đầu khi search
  };

  // Tính toán data hiển thị cho trang hiện tại
  const offset = currentPage * itemsPerPage;
  const currentPageData = searchData?.slice(offset, offset + itemsPerPage) || [];
  const pageCount = Math.ceil((searchData?.length || 0) / itemsPerPage);

  const handlePageClick = (event: { selected: number }) => {
    setCurrentPage(event.selected);
  };

  const handleAgree = async (id: number) => {
    try {
      // Hiển thị thông báo xác nhận trước khi xóa
      const result = await Swal.fire({
        title: "Bạn chắc chắn muốn ẩn bài viết này?",
        text: "Hành động này không thể hoàn tác!",
        icon: "warning",
        showCancelButton: true,
        confirmButtonColor: "#3085d6",
        cancelButtonColor: "#d33",
        confirmButtonText: "Có!",
        cancelButtonText: "Hủy",
      });

      if (result.isConfirmed) {
        // Gọi API để xóa bài viết
        await axios.post(
          `http://localhost:8080/chat-application/v1/post/changeVisible?id=${id}`
        );

        // Hiển thị thông báo thành công
        Swal.fire({
          timer: 1000,
          icon: "success",
          title: "Đã ẩn!",
          text: "Bài viết đã được ẩn.",
          showConfirmButton: false,
        });
      }
    } catch (error) {
      // Hiển thị thông báo lỗi
      Swal.fire("Lỗi!", "Đã xảy ra lỗi khi xóa bài viết.", "error");
    }
  };

  const columns = [
    {
      name: "STT",
      cell: (_row: Blog, index?: number) => {
        return offset + (index ?? 0) + 1;
      },
      width: "70px",
      sortable: false,
    },
    {
      name: "Tiêu đề",
      cell: (row: Blog) => row.caption,
      sortable: true,
    },
    {
      name: "Tên người tạo",
      cell: (row: Blog) => row.reporterUsername,
      sortable: true,
    },
    {
      name: "Vi phạm",
      cell: (row: Blog) => row.context,
      sortable: true,
    },
    {
      name: "Ngày báo cáo",
      cell: (row: Blog) => format(parseISO(row.createdAt), "dd/MM/yyyy HH:mm:ss"),
      sortable: true,
    },
    {
      name: "Phê duyệt",
      cell: (row: Blog) => (
              <button
                onClick={() => handleAgree(row.reportedPostId)}
                style={{
                  border: "none",
                  background: "none",
                  cursor: "pointer",
                  fontSize: "22px",
                }}
                title="Duyệt báo cáo"
              >
                <FaKeyboard style={{ color: "green" }} />
              </button>
      ),
      ignoreRowClick: true,
      allowOverflow: true,
      button: true,
      width: "109px",
    },
  ];

  return (
    <div className={styles.container}>
      <input
        type="text"
        title="Keyword trong tiêu đề và mô tả ngắn"
        onChange={handleSearch}
        placeholder="Tìm kiếm..."
        className="search-input"
        style={{
          position: "absolute",
          top: "5px",
          left: "10px",
          width: "20%",
          padding: "10px",
          borderRadius: "5px",
          border: "1px solid #ccc",
        }}
      />
      <h2 className={styles.heading}>Danh sách báo cáo bài viết</h2>
      {loading && <p style={{ textAlign: "center" }}>Đang tải...</p>}
      {error && <p className={styles.error}>{error}</p>}
      {!loading && !error && (
        <div className={styles.dataTable}>
          {/* <DataTable columns={columns} data={blogs} pagination /> */}
          <DataTable
            columns={columns}
            data={currentPageData}
            highlightOnHover
            noDataComponent="Không có dữ liệu !"
            customStyles={{
              headCells: {
                style: {
                  fontSize: "17px",
                  background: "#ffffff",
                  color: "#1f2937",
                  textAlign: "left",
                  fontWeight: "bold",
                  borderBottom: "2px solid #e5e7eb",
                },
              },
              cells: {
                style: {
                  borderCollapse: "collapse",
                  fontSize: "15px",
                  whiteSpace: "normal",
                  wordWrap: "break-word",
                  height: "auto",
                },
              },
            }}
          />
          {pageCount > 0 && (
            <div style={{ 
              display: "flex", 
              justifyContent: "center", 
              alignItems: "center",
              gap: "20px",
              marginTop: "30px",
              padding: "20px 0"
            }}>
              <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
                <span style={{ color: "#6b7280", fontSize: "14px" }}>Rows per page:</span>
                <select
                  value={itemsPerPage}
                  onChange={(e) => {
                    setItemsPerPage(Number(e.target.value));
                    setCurrentPage(0);
                  }}
                  style={{
                    padding: "6px 12px",
                    border: "1px solid #e5e7eb",
                    borderRadius: "6px",
                    fontSize: "14px",
                    cursor: "pointer",
                    outline: "none"
                  }}
                >
                  <option value={10}>10</option>
                  <option value={20}>20</option>
                  <option value={30}>30</option>
                  <option value={50}>50</option>
                </select>
                <span style={{ color: "#6b7280", fontSize: "14px", marginLeft: "10px" }}>
                  {offset + 1}-{Math.min(offset + itemsPerPage, searchData?.length || 0)} of {searchData?.length || 0}
                </span>
              </div>
              <ReactPaginate
                breakLabel="..."
                nextLabel=">"
                onPageChange={handlePageClick}
                pageRangeDisplayed={5}
                pageCount={pageCount}
                previousLabel="<"
                renderOnZeroPageCount={null}
                containerClassName={styles.pagination}
                activeClassName={styles.active}
                pageClassName={styles.pageItem}
                pageLinkClassName={styles.pageLink}
                previousClassName={styles.pageItem}
                previousLinkClassName={styles.pageLink}
                nextClassName={styles.pageItem}
                nextLinkClassName={styles.pageLink}
                breakClassName={styles.pageItem}
                breakLinkClassName={styles.pageLink}
              />
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default ReportBlog;
