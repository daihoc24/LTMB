import React, { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import styles from "./userList.module.css";
import { FaLock, FaUnlock } from "react-icons/fa";
import DataTable from "react-data-table-component";
import Swal from "sweetalert2";
import { useDispatch, useSelector } from "react-redux";
import { RootState } from "../../reduxStore/Store";
import { MdAdminPanelSettings } from "react-icons/md";
import ReactPaginate from "react-paginate";

interface User {
  id: number;
  birthday: string;
  username: string;
  email: string;
  status: number;
}

function UserList() {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string>("");
  const [searchData, setSearchData] = useState<User[]>();
  const [currentPage, setCurrentPage] = useState<number>(0);
  const [itemsPerPage, setItemsPerPage] = useState<number>(10);

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const response = await axios.post(
        `http://localhost:8080/chat-application/v1/users/alluser`,
        {},
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("authToken")}`,
          },
        },
      );
      console.log("call api succcess");
      setUsers(response.data);
      setSearchData(response.data);
      console.log(response.data);
    } catch (err) {
      console.log("lỗi");
      if (axios.isAxiosError(err)) {
        const errorMsg =
          typeof err.response?.data === "string"
            ? err.response.data
            : "Không thể lấy danh sách user. Vui lòng thử lại sau";
        setError(errorMsg);
      } else {
        setError("Đã xảy ra lỗi. Vui lòng thử lại sau.");
      }
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (event: React.ChangeEvent<HTMLInputElement>) => {
    const newData = users.filter((row) => {
      return (
        row.username.toLowerCase().includes(event.target.value.toLowerCase()) ||
        row.email.toLowerCase().includes(event.target.value.toLowerCase())
      );
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

  const toggleLockStatus = async (username: string, isLock: boolean) => {
    setLoading(true);
    console.log("locked", username, isLock);
    try {
      await axios.post(
        `http://localhost:8080/chat-application/v1/users/lockAccount`,
        { username: username },
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("authToken")}`,
          },
        },
      );
      if (isLock)
        Swal.fire({
          title: "Đã khóa tài khoản!",
          icon: "success",
          showConfirmButton: false,
          timer: 1000,
          toast: true,
          timerProgressBar: true,
        });
      else
        Swal.fire({
          title: "Đã mở tài khoản!",
          icon: "success",
          showConfirmButton: false,
          timer: 1000,
          toast: true,
          timerProgressBar: true,
        });
      fetchUsers();
    } catch (err) {
      if (axios.isAxiosError(err)) {
        const errorMsg =
          typeof err.response?.data === "string"
            ? err.response.data
            : "Không thể cập nhật trạng thái tài khoản. Vui lòng thử lại sau";
        setError(errorMsg);
      } else {
        setError("Đã xảy ra lỗi. Vui lòng thử lại sau.");
      }
    } finally {
      setLoading(false);
    }
  };

  const columns = [
    {
      name: "STT",
      cell: (_row: User, index?: number) => {
        return offset + (index ?? 0) + 1;
      },
      width: "70px",
      sortable: false,
    },
    {
      name: "Tên Tài Khoản",
      selector: (row: User) => row.username,
      sortable: true,
      width: "170px",
    },
    {
      name: "Tên Đăng Nhập",
      selector: (row: User) => row.username,
      sortable: true,
      width: "180px",
    },
    {
      name: "Email",
      selector: (row: User) => row.email,
      sortable: true,
    },
    {
      name: "Ngày sinh",
      selector: (row: User) => row.birthday,
    },
    {
      name: "Trạng thái",
      cell: (row: User) => (
        <button
          style={{ margin: "auto", cursor: "pointer" }}
          onClick={() => {
            if (row.status == 1) {
              toggleLockStatus(row.username, true);
            } else if (row.status == 0) {
              toggleLockStatus(row.username, false);
            }
          }}
        >
          {row.status === 0 ? (
            <FaLock style={{ color: "red" }} />
          ) : (
            <FaUnlock />
          )}
        </button>
      ),
      sortable: true,
      width: "150px",
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
      <h2>Danh Sách Người Dùng</h2>
      {loading && <p>Đang tải...</p>}
      {error && <p style={{ color: "red" }}>{error}</p>}

      <div className={styles.dataTable}>
        <DataTable
          columns={columns}
          data={currentPageData}
          progressPending={loading}
          highlightOnHover
          noDataComponent="Không có dữ liệu để hiển thị"
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
    </div>
  );
}

export default UserList;
