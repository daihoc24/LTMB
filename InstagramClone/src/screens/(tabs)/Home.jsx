import React, { useCallback, useContext } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  Image,
  TextInput,
  StyleSheet,
  Pressable,
  ScrollView,
  FlatList,
} from 'react-native';
import images from '../../config/images';
import { useState, useEffect } from 'react';
import { IconUserProfile } from '../../components/IconComponents';
import { AuthContext } from '../../context/AuthContext';
import ENDPOINTS from '../../config/endpoints';
import axios from 'axios';
import Modal from 'react-native-modal';
import Ionicons from 'react-native-vector-icons/Ionicons';
import { useIsFocused } from '@react-navigation/native';
import Video from 'react-native-video';
import ConnectedUsersList from '../../components/ConnectedUsersList';
import { OneSignal } from 'react-native-onesignal';
import LikeButton from './like';
import { handleError } from '../../utils/handleError';
import PostComponent from '../../components/PostComponent';
import { ActivityIndicator, Avatar, useTheme } from 'react-native-paper';
import AvatarComponent from '../../components/AvatarComponent';

const Home = ({ navigation, route }) => {
  const {
    tokenContext,
    setIdContext,
    idContext,
    setUsernameContext,
    setEmailContext,
    setCreatedAtContext,
    setBirthdayContext,
    setPrivacyContext,
    setStatusContext,
    setRoleContext,
    setAvatarContext
  } = useContext(AuthContext);

  const theme = useTheme()

  const [isModalVisible, setModalVisible] = useState(false);
  const [newCaption, setNewCaption] = useState(); // dùng cho edit caption
  const [commentInputs, setCommentInputs] = useState({}); // comment riêng từng post
  const [isModalEditVisible, setModalEditVisible] = useState(false);
  const [loadingFollowingList, setLoadingFollowingList] = useState(false);
  const [loadingPost, setLoadingPost] = useState(false);
  const [yourComment, setYourComment] = useState();
  const [medias, setMedias] = useState([]);
  const [follow, setFollow] = useState([]);
  const [posts, setPosts] = useState([]);
  const [foldersCloudinary, setFoldersCloudinary] = useState([]);
  const [selectedPostId, setSelectedPostId] = useState(null);
  const [selectedEditPostId, setSelectedEditPostId] = useState(null);
  const [user, setUser] = useState();
  const [commentsByPost, setCommentsByPost] = useState({});
  const [editingCommentId, setEditingCommentId] = useState(null);
  const [editingCommentText, setEditingCommentText] = useState('');
  const isFocused = useIsFocused();

  // Phương thức lấy thông tin người dùng
  const fetchUserInfo = async () => {
    const endpoint = ENDPOINTS.USER.MY_INFORMATION;
    try {
      const response = await axios.get(endpoint, {
        headers: {
          Authorization: `Bearer ${tokenContext}`,
        },
      });
      return response.data;
    } catch (error) {
      console.log(`Lỗi khi gọi API: ${endpoint}`); // Log endpoint
      handleError(error);
    }
  };

  // Phương thức lấy danh sách người dùng theo dõi
  const fetchFollowingUsers = async (userInfo) => {
    const getFollowingEndpoint = ENDPOINTS.CHAT.FOLLOWING_USERS;
    try {
      const followingResponse = await axios.post(getFollowingEndpoint, userInfo, {
        headers: { Authorization: `Bearer ${tokenContext}` },
      });
      return followingResponse.data;
    } catch (error) {
      console.log(`Lỗi khi gọi API: ${getFollowingEndpoint}`); // Log endpoint
      handleError(error);
    }
  };

  // Phương thức lấy tất cả bài viết của nhiều người dùng
  const fetchPostsByFollowingUsers = async () => {
    const findAllMultipleUserEndpoint = ENDPOINTS.POST.FIND_ALL_MULTIPLE_USER;
    try {
      const postResponse = await axios.post(findAllMultipleUserEndpoint);
      return postResponse.data.result;
    } catch (error) {
      console.log(`Lỗi khi gọi API: ${findAllMultipleUserEndpoint}`); // Log endpoint
      handleError(error);
    }
  };

  // Phương thức lấy media từ Cloudinary
  const fetchMediaFromCloudinary = async (folders) => {
    const multipleMediaEndpoint = ENDPOINTS.CLOUDINARY.FIND_ALL_MULTIPLE;
    if (!folders || folders.length === 0) return [];
    try {
      const mediaResponse = await axios.post(multipleMediaEndpoint, folders, {
        headers: { Authorization: `Bearer ${tokenContext}` },
      });
      return mediaResponse?.data?.result || [];
    } catch (error) {
      console.log(`Lỗi khi gọi API: ${multipleMediaEndpoint}`); // Log endpoint
      handleError(error);
      return [];
    }
  };

  // Phương thức chính để fetch dữ liệu
  const fetchData = async () => {
    setLoadingFollowingList(true)
    setLoadingPost(true)
    if (!tokenContext) {
      console.log('Token is not available');
      return;
    } else console.log('Token is existed:', tokenContext);

    try {
      const userInfoResponse = await fetchUserInfo();
      if (userInfoResponse && userInfoResponse.code === 200 && userInfoResponse.result) {
        const userInfo = userInfoResponse.result;
        console.log(`Userinformation: ${JSON.stringify(userInfo, null, 2)}`);
        setUser(userInfo);
        // Lưu thông tin vào Context
        setIdContext(userInfo.id);
        setUsernameContext(userInfo.username);
        setEmailContext(userInfo.email);
        setCreatedAtContext(userInfo.createdAt);
        setBirthdayContext(userInfo.birthday);
        setAvatarContext(userInfo.avatar);
        setPrivacyContext(userInfo.privacy);
        setStatusContext(userInfo.status);
        setRoleContext({ roles: userInfo.roles });

        console.log('User  information loaded successfully.');
        OneSignal.initialize('672c61cb-8e38-40a0-9d50-d0cc76dc03fe');
        OneSignal.login(userInfo.id);
        OneSignal.User.pushSubscription.optIn();

        // Gọi API lấy danh sách following
        const followingList = await fetchFollowingUsers(userInfo);
        setFollow(followingList || []);
        setLoadingFollowingList(false)

        // Lấy danh sách post dựa trên following
        const followingUserIds = followingList.map(value => ({ id: value.id }));
        followingUserIds.push({ id: userInfo.id }); // Thêm chính người dùng hiện tại

        const postsResponse = await fetchPostsByFollowingUsers();
        const safePosts = Array.isArray(postsResponse) ? postsResponse : [];
        const visiblePosts = safePosts
          .filter(post => post?.visible === true)
          // Sắp xếp bài mới nhất lên đầu (fallback nếu backend chưa sort)
          .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
        setPosts(visiblePosts);
        console.log(`posts: ${visiblePosts.length} posts`)

        // Lấy media dựa trên các post đã lấy
        let folders = visiblePosts.map(value => `posts/${value.user?.id}/${value.id}`);
        setFoldersCloudinary(folders);
        console.log(`folder: ${foldersCloudinary}`)

        const mediasResponse = await fetchMediaFromCloudinary(folders);
        setMedias(mediasResponse || []);
        console.log('mediaResponse', mediasResponse);
        console.log(mediasResponse?.length || 0)
        console.log(mediasResponse?.[0])
        console.log(mediasResponse?.[1])

        // Lấy comment cho từng post
        await fetchCommentsForPosts(visiblePosts);
      } else {
        console.log('Unexpected response format:', userInfoResponse);
      }
    } catch (error) {
      handleError(error);
    }
    setLoadingPost(false)
  };

  const fetchCommentsForPosts = async (postList) => {
    if (!postList || postList.length === 0) return;
    try {
      const results = await Promise.all(
        postList.map(async (post) => {
          try {
            const res = await axios.get(`${ENDPOINTS.COMMENT.LIST}?postId=${post.id}`);
            return { postId: post.id, comments: res.data || [] };
          } catch (e) {
            // Không để 1 post lỗi làm fail toàn bộ Promise.all
            // 404: coi như chưa có API/comment cho post đó -> return rỗng, không log spam
            if (e?.response?.status === 404) {
              return { postId: post.id, comments: [] };
            }
            console.log(`[comment] load failed for postId=${post.id}:`, e?.message);
            return { postId: post.id, comments: [] };
          }
        })
      );
      const mapped = {};
      results.forEach(({ postId, comments }) => {
        mapped[postId] = comments;
      });
      setCommentsByPost(mapped);
    } catch (error) {
      // tránh crash khi error object lạ, và tránh log spam
      try { handleError(error); } catch (e) {}
    }
  };

  const handleSendComment = async (postId) => {
    const content = commentInputs[postId];
    if (!content || !content.trim()) return;
    try {
      const payload = {
        postId,
        userId: idContext,
        content: content.trim(),
      };
      const res = await axios.post(ENDPOINTS.COMMENT.CREATE, payload);
      const newCmt = res.data;
      setCommentsByPost((prev) => {
        const current = prev[postId] || [];
        return { ...prev, [postId]: [...current, newCmt] };
      });
      setCommentInputs((prev) => ({ ...prev, [postId]: '' }));
    } catch (error) {
      console.log('Lỗi khi gửi comment', error);
      try { handleError(error); } catch (e) { console.log('handleError failed', e?.message); }
    }
  };

  const handleStartEditComment = (comment) => {
    setEditingCommentId(comment.id);
    setEditingCommentText(comment.content);
  };

  const handleCancelEditComment = () => {
    setEditingCommentId(null);
    setEditingCommentText('');
  };

  const handleSaveEditComment = async (postId) => {
    if (!editingCommentId) return;
    const newText = editingCommentText?.trim();
    if (!newText) {
      handleCancelEditComment();
      return;
    }
    try {
      await axios.put(`${ENDPOINTS.COMMENT.UPDATE}/${editingCommentId}`, {
        content: newText,
      });
      setCommentsByPost(prev => {
        const list = prev[postId] || [];
        const updated = list.map(c =>
          c.id === editingCommentId ? { ...c, content: newText } : c,
        );
        return { ...prev, [postId]: updated };
      });
      handleCancelEditComment();
    } catch (error) {
      console.log('Lỗi khi chỉnh sửa comment', error);
      try { handleError(error); } catch (e) { }
    }
  };

  const handleDeleteComment = async (postId, commentId) => {
    try {
      await axios.delete(`${ENDPOINTS.COMMENT.DELETE}/${commentId}`);
      setCommentsByPost(prev => {
        const list = prev[postId] || [];
        const updated = list.filter(c => c.id !== commentId);
        return { ...prev, [postId]: updated };
      });
      if (editingCommentId === commentId) {
        handleCancelEditComment();
      }
    } catch (error) {
      console.log('Lỗi khi xóa comment', error);
      try { handleError(error); } catch (e) { }
    }
  };

  useEffect(() => {
    if (isFocused) {
      fetchData(); // Lấy lại bài posts khi quay về màn hình Home
    }
  }, [isFocused]);

  const renderItem = ({ item: url, index: idxChild }) => {
    console.log('loop opp opoo')
    if (url.endsWith('.jpg') || url.endsWith('.png')) {
      return (
        <Image
          key={idxChild}
          source={{ uri: url }}
          style={styles.selectedImage}
          resizeMode="cover"
        />
      );
    } else {
      return (
        <Video
          key={idxChild}
          style={[styles.selectedVideo]}
          source={{ uri: url }}
          controls={true}
          resizeMode="contain"
          onBuffer={this.onBuffer}
          onError={this.videoError}
        />
      );
    }
  };

  const toggleModal = (postId, caption) => {
    setSelectedPostId(postId);
    setNewCaption(caption);
    setModalVisible(!isModalVisible);
  };

  const toggleModalEditPost = postId => {
    setModalEditVisible(!isModalEditVisible);
  };

  const handleEdit = async () => {
    setNewCaption('');
    console.log('Edit option clicked: ' + selectedPostId);
    console.log('new caption: ' + newCaption);
    const post = {
      id: selectedPostId,
      caption: newCaption,
    };

    const updateCaptionEndpoint = ENDPOINTS.POST.UPDATE_CAPTION;
    await axios.post(updateCaptionEndpoint, post, {
      headers: {
        Authorization: `Bearer ${tokenContext}`,
      },
    });
    fetchData();
    toggleModal();
    toggleModalEditPost();
  };

  const handleDelete = async () => {
    console.log('Delete option clicked: ' + selectedPostId);
    const post = {
      id: selectedPostId,
      visible: false,
    };

    const hiddenEndpoint = ENDPOINTS.POST.UPDATE_VISIBLE;
    await axios.post(hiddenEndpoint, post, {
      headers: {
        Authorization: `Bearer ${tokenContext}`,
      },
    });

    fetchData();
    toggleModal(); // Close modal after action
  };


  return (
    <View className="w-full h-full flex justify-center items-center bg-white">
      <ScrollView className="w-full" showsVerticalScrollIndicator={false}>
        {/* new feeds */}
        {loadingFollowingList ?
          (<ActivityIndicator animating={true} color={theme.colors.primary} />) :
          (<ConnectedUsersList styleGroup={`my-2`} list={follow} />)}
        {/* <PostComponent /> */}

        {/* Post */}
        {loadingPost ?
          (<ActivityIndicator animating={true} color={theme.colors.primary} />) :
          (
            <View>
              {/* Hiển thị dữ liệu postsInUI */}
              {posts.length > 0 ? (
                posts.map((post, index) => (
                  <View className="flex flex-column w-full py-3" key={post.id}>
                    {/* Header post */}
                    <View className="flex flex-row w-full justify-between items-center px-3 mb-3">
                      {/* Header left */}
                      <TouchableOpacity className="flex flex-row items-center">
                        {/* <Avatar.Image source={
                                post.user.avatar? { uri: post.user.avatar } : require('../../assets/avatarDefine.jpg')
                              } size={50} /> */}
                        <AvatarComponent user={post.user} size={40} />

                          <View className="p-2 flex flex-column">
                            <View className="flex flex-row items-center">
                              <Text className="font-semibold text-lg" style={{color: '#111827'}}>
                                {post.user.username}
                              </Text>
                              <Image
                                className="ml-1"
                                source={images.icon_verify}
                                style={{ width: 25, height: 25 }}
                                resizeMode="contain"
                              />
                            </View>

                            {/* Sub title */}
                            <Text className="text-sm" style={{color: '#6b7280'}}>This is subtitle</Text>
                          </View>
                      </TouchableOpacity>

                      {/* Header right: chỉ cho chủ post được mở menu sửa/xóa */}
                      {idContext && String(post.user.id).toLowerCase() === String(idContext).toLowerCase() && (
                        <TouchableOpacity
                          onPress={() => toggleModal(post.id, post.caption)}
                          style={styles.optionsButton}>
                          <Image
                            source={images.icon_triple_dot}
                            style={{
                              width: 24,
                              height: 24,
                            }}
                          />
                        </TouchableOpacity>
                      )}
                    </View>

                    {/* Modal */}
                    <Modal
                      isVisible={isModalVisible}
                      onBackdropPress={() => setModalVisible(false)}
                      backdropOpacity={0.1}
                      style={styles.modal}>
                      <View style={styles.modalContent}>
                        <TouchableOpacity
                          onPress={() => toggleModalEditPost()}
                          style={styles.option}>
                          <Ionicons name="pencil-outline" size={20} />
                          <Text style={styles.optionText}>Edit</Text>
                        </TouchableOpacity>
                        <TouchableOpacity
                          onPress={() => handleDelete()}
                          style={styles.option}>
                          <Ionicons name="trash-outline" size={20} color="red" />
                          <Text style={[styles.optionText, { color: 'red' }]}>
                            Delete
                          </Text>
                        </TouchableOpacity>
                      </View>
                    </Modal>

                    {/* {Modal edit  post} */}
                    <Modal
                      isVisible={isModalEditVisible}
                      onBackdropPress={() => setModalEditVisible(false)}
                      backdropOpacity={0.1}
                      style={styles.modal}>
                      <View style={styles.modalEditContent}>
                        <View
                          style={{
                            display: 'flex',
                            flexDirection: 'row',
                            alignItems: 'center',
                            justifyContent: 'space-between',
                          }}>
                          <View
                            style={{
                              display: 'flex',
                              flexDirection: 'row',
                              alignItems: 'center',
                            }}>
                            <TouchableOpacity
                              onPress={() => setModalEditVisible(false)}>
                              <Ionicons
                                name="arrow-back-outline"
                                size={25}></Ionicons>
                            </TouchableOpacity>
                            <Text style={{ fontSize: 20, marginLeft: 10, color: '#111827' }}>
                              Sửa đổi
                            </Text>
                          </View>

                          <TouchableOpacity onPress={() => handleEdit()}>
                            <Ionicons name="checkmark-outline" size={25}></Ionicons>
                          </TouchableOpacity>
                        </View>

                        <TouchableOpacity onPress={() => handleEdit()}>
                          <Ionicons name="checkmark-outline" size={25}></Ionicons>
                        </TouchableOpacity>
                      </View>
                      <TextInput
                        className="ml-1"
                        style={{color: '#111827'}}
                        placeholder="Write a caption..."
                        placeholderTextColor="#9ca3af"
                        onChangeText={text => setNewCaption(text)}
                        value={newCaption}
                      />
                    </Modal>

                    <View
                      style={{
                        flexl: 1,
                        justifyContent: 'center',
                        alignItems: 'center',
                        marginBottom: 5,
                      }}>
                      <FlatList
                        data={(medias?.[index] ? medias[index].slice().reverse() : [])} // Chỉ render danh sách media tương ứng với index
                        renderItem={renderItem}
                        keyExtractor={(item, idxChild) => `${index} ${idxChild}`}
                        horizontal
                        inverted
                        pagingEnabled
                        bounces={false}
                      />
                    </View>

                    {/* Footer post */}
                    <View className="flex flew-column">
                      <View className="w-full flex flex-column justify-between px-3">
                        {/* React row */}
                        <View className="w-24 flex flex-row justify-between items-center mb-2">
                          <LikeButton
                            postId={post.id} />
                          <TouchableOpacity className="">
                            <Image
                              source={images.icon_message}
                              style={{
                                width: 25,
                                height: 25,
                                transform: [{ scaleX: -1 }],
                              }}
                            />
                          </TouchableOpacity>
                          <TouchableOpacity className="">
                            <Image
                              source={images.icon_share}
                              style={{
                                width: 25,
                                height: 25,
                              }}
                            />
                          </TouchableOpacity>
                        </View>
                      </View>

                      {/* Comment row */}
                      {/* caption lùi vào cùng biên với avatar/icon */}
                      <Text className="w-full mb-2 px-3" style={{color: '#111827'}}>{post.caption}</Text>
                      <View style={styles.commentInputRow}>
                        <View style={styles.commentAvatarPlaceholder}>
                          {user?.avatar ? (
                            <Image
                              source={{ uri: user.avatar }}
                              style={{ width: 32, height: 32, borderRadius: 16 }}
                              resizeMode="cover"
                            />
                          ) : (
                            <Text style={styles.commentAvatarText}>+</Text>
                          )}
                        </View>
                        <TextInput
                          style={styles.commentInput}
                          placeholder="Write a comment..."
                          onChangeText={text =>
                            setCommentInputs(prev => ({
                              ...prev,
                              [post.id]: text,
                            }))
                          }
                          value={commentInputs[post.id] || ''}
                          placeholderTextColor="#9ca3af"
                        />
                        <TouchableOpacity
                          style={styles.sendButton}
                          onPress={() => handleSendComment(post.id)}>
                          <Text style={{ color: theme.colors.primary, fontWeight: '600' }}>Gửi</Text>
                        </TouchableOpacity>
                      </View>
                      {commentsByPost[post.id]?.length > 0 && (
                        <View style={styles.commentList}>
                          {commentsByPost[post.id].map((cmt, idx) => {
                            const isOwner =
                              cmt.userId &&
                              idContext &&
                              String(cmt.userId).toLowerCase() === String(idContext).toLowerCase();
                            const displayName = cmt.username || (cmt.userId ? String(cmt.userId).slice(0, 6) : 'user');
                            const avatarText = displayName.slice(0, 2);
                            const isEditing = editingCommentId === cmt.id;
                            return (
                              <View key={`${cmt.id || idx}`} style={styles.commentItem}>
                                <View style={styles.commentAvatar}>
                                  {cmt.avatar ? (
                                    <Image
                                      source={{ uri: cmt.avatar }}
                                      style={{ width: 28, height: 28, borderRadius: 14 }}
                                      resizeMode="cover"
                                    />
                                  ) : (
                                    <Text style={styles.commentAvatarText}>{avatarText}</Text>
                                  )}
                                </View>
                                <View style={styles.commentContent}>
                                  {isEditing ? (
                                    <View style={styles.commentEditRow}>
                                      <TextInput
                                        style={styles.commentEditInput}
                                        value={editingCommentText}
                                        onChangeText={setEditingCommentText}
                                        autoFocus
                                      />
                                      <TouchableOpacity onPress={() => handleSaveEditComment(post.id)}>
                                        <Text style={styles.commentEditSave}>Lưu</Text>
                                      </TouchableOpacity>
                                      <TouchableOpacity onPress={handleCancelEditComment}>
                                        <Text style={styles.commentEditCancel}>Hủy</Text>
                                      </TouchableOpacity>
                                    </View>
                                  ) : (
                                    <View style={styles.commentBubble}>
                                      <View style={styles.commentBubbleRow}>
                                        <Text style={styles.commentText}>
                                          <Text style={styles.commentUser}>{displayName}</Text>
                                          {`  ${cmt.content}`}
                                        </Text>
                                        {isOwner && (
                                          <View style={styles.commentActions}>
                                            <TouchableOpacity
                                              style={styles.commentIconButton}
                                              onPress={() => handleStartEditComment(cmt)}>
                                              <Ionicons
                                                name="pencil-outline"
                                                size={16}
                                                color="#6b7280"
                                              />
                                            </TouchableOpacity>
                                            <TouchableOpacity
                                              style={styles.commentIconButton}
                                              onPress={() => handleDeleteComment(post.id, cmt.id)}>
                                              <Ionicons
                                                name="trash-outline"
                                                size={16}
                                                color="#ef4444"
                                              />
                                            </TouchableOpacity>
                                          </View>
                                        )}
                                      </View>
                                    </View>
                                  )}
                                </View>
                              </View>
                            );
                          })}
                        </View>
                      )}

                      {/* <View
                        style={{
                          flexl: 1,
                          justifyContent: 'center',
                          alignItems: 'center',
                          marginBottom: 5,
                        }}>
                        <FlatList
                          data={medias[index]} // Chỉ render danh sách media tương ứng với index
                          renderItem={renderItem}
                          keyExtractor={(item, idxChild) => `${index} ${idxChild}`}
                          horizontal
                          pagingEnabled
                          bounces={false}
                        />
                      </View> */}

                      {/* Footer post */}
                      
                    </View>
                  </View>

                  // <PostComponent post={post} />
                ))
              ) : (
                <Text style={styles.welcome}>
                  Chào mừng bạn đến với chúng tôi. Hãy chia sẻ và kết nối với mọi
                  người
                </Text>
              )}
            </View>
          )}
        {/* navigation bottom */}
      </ScrollView >
    </View >
  );
};

const styles = StyleSheet.create({
  welcome: {
    height: 500,
    textAlign: 'center',
    fontSize: 20,
    color: '#111827',
  },
  icons: {
    width: 28,
    height: 28,
  },
  selectedVideo: {
    width: 412,
    height: 600,
    borderRadius: 5,
    shadowColor: 'black',
    shadowOffset: { width: 0, height: 5 },
    shadowOpacity: 0.8,
    shadowRadius: 10,
  },
  selectedImage: {
    width: 415,
    height: 600,
    borderRadius: 5,
    shadowColor: 'black',
    shadowOffset: { width: 0, height: 5 },
    shadowOpacity: 0.8,
    shadowRadius: 10,
  },
  optionsButton: {
    padding: 10,
  },
  modal: {
    justifyContent: 'flex-end',
    margin: 0,
  },
  modalContent: {
    backgroundColor: 'white',
    padding: 20,
    borderTopLeftRadius: 20,
    borderTopRightRadius: 20,
    elevation: 5,
  },
  modalEditContent: {
    flex: 1,
    backgroundColor: 'white',
    padding: 20,
    borderTopLeftRadius: 20,
    borderTopRightRadius: 20,
    elevation: 5,
  },
  option: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 15,
  },
  optionText: {
    marginLeft: 10,
    fontSize: 18,
    color: '#111827',
  },
  commentInputRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 12,
    paddingVertical: 6,
    gap: 8,
  },
  commentAvatarPlaceholder: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: '#e5e7eb',
    alignItems: 'center',
    justifyContent: 'center',
  },
  commentAvatarText: {
    fontWeight: '700',
    color: '#6b7280',
  },
  commentInput: {
    flex: 1,
    paddingHorizontal: 10,
    paddingVertical: 8,
    borderRadius: 10,
    backgroundColor: '#f3f4f6',
    fontSize: 14,
    color: '#111827',
  },
  sendButton: {
    paddingHorizontal: 8,
    paddingVertical: 6,
  },
  commentList: {
    paddingHorizontal: 12,
    paddingBottom: 8,
    marginTop: 4,
  },
  commentItem: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    marginBottom: 6,
  },
  commentAvatar: {
    width: 28,
    height: 28,
    borderRadius: 14,
    backgroundColor: '#e5e7eb',
    alignItems: 'center',
    justifyContent: 'center',
  },
  commentContent: {
    flex: 1,
    marginLeft: 8,
  },
  commentBubble: {
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: 14,
    backgroundColor: '#f3f4f6',
  },
  commentBubbleRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 6,
  },
  commentUser: {
    fontWeight: '700',
    color: '#111827',
    fontSize: 13,
  },
  commentText: {
    flex: 1,
    color: '#111827',
    fontSize: 13,
    lineHeight: 18,
  },
  commentActions: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  commentIconButton: {
    paddingHorizontal: 4,
    paddingVertical: 2,
  },
  commentEditRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  commentEditInput: {
    flex: 1,
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#e5e7eb',
    fontSize: 13,
    color: '#111827',
  },
  commentEditSave: {
    marginLeft: 8,
    fontSize: 12,
    color: '#10b981',
    fontWeight: '600',
  },
  commentEditCancel: {
    marginLeft: 4,
    fontSize: 12,
    color: '#9ca3af',
  },
});
export default Home;
