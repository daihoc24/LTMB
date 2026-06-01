import React, { useState, useContext, useEffect } from 'react';
import {
  View,
  Text,
  Image,
  StyleSheet,
  Alert,
  TouchableOpacity,
  ScrollView,
  Pressable,
  Modal,
  FlatList,
  TextInput,
  Dimensions,
} from 'react-native';
import axios from 'axios';
import FontAwesome from 'react-native-vector-icons/FontAwesome';
import FontAwesome6 from 'react-native-vector-icons/FontAwesome6';
import Fontisto from 'react-native-vector-icons/Fontisto';
import Ionicons from 'react-native-vector-icons/Ionicons';
import { AuthContext } from '../../context/AuthContext';
import UserSuggestion from '../../components/UserSuggestion';
import ENDPOINTS from '../../config/endpoints';
import { handleError } from '../../utils/handleError';
import QRCode from 'react-native-qrcode-svg';
import { useNavigation, useRoute, useIsFocused } from '@react-navigation/native';
import { ActivityIndicator } from 'react-native-paper';
import Video from 'react-native-video';
import RNModal from 'react-native-modal';
import AvatarComponent from '../../components/AvatarComponent';
import LikeButton from '../(tabs)/like';
import images from '../../config/images';

const Profile = ({ user, isUser }) => {
  // const images = [
  //   {
  //     id: '1',
  //     quantity: 1,
  //     uri: 'https://i.pinimg.com/736x/ff/d8/10/ffd8109392e5aa39b56f341f4a388ee9.jpg',
  //   },
  //   {
  //     id: '2',
  //     quantity: 2,
  //     uri: 'https://i.pinimg.com/736x/5d/7f/5f/5d7f5f33f763c18b03fc6cd9836a423d.jpg',
  //   },
  //   {
  //     id: '3',
  //     quantity: 1,
  //     uri: 'https://i.pinimg.com/736x/c6/12/8a/c6128ae7a90bed67e450fa6376891273.jpg',
  //   },
  //   {
  //     id: '4',
  //     quantity: 10,
  //     uri: 'https://i.pinimg.com/736x/b7/c2/31/b7c2314472307131946d9b255c3c06f7.jpg',
  //   },
  //   {
  //     id: '5',
  //     quantity: 5,
  //     uri: 'https://i.pinimg.com/736x/2d/e2/ca/2de2caefc8094a183aaa3a070e9ed420.jpg',
  //   },
  //   {
  //     id: '6',
  //     quantity: 1,
  //     uri: 'https://i.pinimg.com/736x/2d/e2/ca/2de2caefc8094a183aaa3a070e9ed420.jpg',
  //   },
  //   {
  //     id: '7',
  //     quantity: 1,
  //     uri: 'https://i.pinimg.com/736x/2d/e2/ca/2de2caefc8094a183aaa3a070e9ed420.jpg',
  //   },
  //   {
  //     id: '8',
  //     quantity: 2,
  //     uri: 'https://i.pinimg.com/736x/2d/e2/ca/2de2caefc8094a183aaa3a070e9ed420.jpg',
  //   },
  //   {
  //     id: '9',
  //     quantity: 1,
  //     uri: 'https://i.pinimg.com/736x/2d/e2/ca/2de2caefc8094a183aaa3a070e9ed420.jpg',
  //   },
  //   {
  //     id: '10',
  //     quantity: 10,
  //     uri: 'https://i.pinimg.com/736x/2d/e2/ca/2de2caefc8094a183aaa3a070e9ed420.jpg',
  //   },
  //   {
  //     id: '11',
  //     quantity: 5,
  //     uri: 'https://i.pinimg.com/736x/2d/e2/ca/2de2caefc8094a183aaa3a070e9ed420.jpg',
  //   },
  //   {
  //     id: '12',
  //     quantity: 1,
  //     uri: 'https://i.pinimg.com/736x/2d/e2/ca/2de2caefc8094a183aaa3a070e9ed420.jpg',
  //   },
  //   {
  //     id: '13',
  //     quantity: 3,
  //     uri: 'https://i.pinimg.com/736x/ff/d8/10/ffd8109392e5aa39b56f341f4a388ee9.jpg',
  //   },
  //   {
  //     id: '14',
  //     quantity: 4,
  //     uri: 'https://i.pinimg.com/736x/5d/7f/5f/5d7f5f33f763c18b03fc6cd9836a423d.jpg',
  //   },
  //   {
  //     id: '15',
  //     quantity: 6,
  //     uri: 'https://i.pinimg.com/736x/c6/12/8a/c6128ae7a90bed67e450fa6376891273.jpg',
  //   },
  //   {
  //     id: '16',
  //     quantity: 7,
  //     uri: 'https://i.pinimg.com/736x/b7/c2/31/b7c2314472307131946d9b255c3c06f7.jpg',
  //   },
  //   {
  //     id: '17',
  //     quantity: 8,
  //     uri: 'https://i.pinimg.com/736x/2d/e2/ca/2de2caefc8094a183aaa3a070e9ed420.jpg',
  //   },
  //   {
  //     id: '18',
  //     quantity: 9,
  //     uri: 'https://i.pinimg.com/736x/ff/d8/10/ffd8109392e5aa39b56f341f4a388ee9.jpg',
  //   },
  //   {
  //     id: '19',
  //     quantity: 2,
  //     uri: 'https://i.pinimg.com/736x/5d/7f/5f/5d7f5f33f763c18b03fc6cd9836a423d.jpg',
  //   },
  //   {
  //     id: '20',
  //     quantity: 3,
  //     uri: 'https://i.pinimg.com/736x/c6/12/8a/c6128ae7a90bed67e450fa6376891273.jpg',
  //   },
  //   {
  //     id: '21',
  //     quantity: 4,
  //     uri: 'https://i.pinimg.com/736x/b7/c2/31/b7c2314472307131946d9b255c3c06f7.jpg',
  //   },
  //   {
  //     id: '22',
  //     quantity: 5,
  //     uri: 'https://i.pinimg.com/736x/2d/e2/ca/2de2caefc8094a183aaa3a070e9ed420.jpg',
  //   },
  //   {
  //     id: '23',
  //     quantity: 6,
  //     uri: 'https://i.pinimg.com/736x/ff/d8/10/ffd8109392e5aa39b56f341f4a388ee9.jpg',
  //   },
  //   {
  //     id: '24',
  //     quantity: 7,
  //     uri: 'https://i.pinimg.com/736x/5d/7f/5f/5d7f5f33f763c18b03fc6cd9836a423d.jpg',
  //   },
  //   {
  //     id: '25',
  //     quantity: 8,
  //     uri: 'https://i.pinimg.com/736x/c6/12/8a/c6128ae7a90bed67e450fa6376891273.jpg',
  //   },
  //   {
  //     id: '26',
  //     quantity: 9,
  //     uri: 'https://i.pinimg.com/736x/b7/c2/31/b7c2314472307131946d9b255c3c06f7.jpg',
  //   },
  //   {
  //     id: '27',
  //     quantity: 10,
  //     uri: 'https://i.pinimg.com/736x/2d/e2/ca/2de2caefc8094a183aaa3a070e9ed420.jpg',
  //   },
  //   {
  //     id: '28',
  //     quantity: 1,
  //     uri: 'https://i.pinimg.com/736x/ff/d8/10/ffd8109392e5aa39b56f341f4a388ee9.jpg',
  //   },
  //   {
  //     id: '29',
  //     quantity: 2,
  //     uri: 'https://i.pinimg.com/736x/5d/7f/5f/5d7f5f33f763c18b03fc6cd9836a423d.jpg',
  //   },
  //   {
  //     id: '30',
  //     quantity: 3,
  //     uri: 'https://i.pinimg.com/736x/c6/12/8a/c6128ae7a90bed67e450fa6376891273.jpg',
  //   },
  //   {
  //     id: '31',
  //     quantity: 4,
  //     uri: 'https://i.pinimg.com/736x/b7/c2/31/b7c2314472307131946d9b255c3c06f7.jpg',
  //   },
  //   {
  //     id: '32',
  //     quantity: 5,
  //     uri: 'https://i.pinimg.com/736x/2d/e2/ca/2de2caefc8094a183aaa3a070e9ed420.jpg',
  //   },
  //   {
  //     id: '33',
  //     quantity: 6,
  //     uri: 'https://i.pinimg.com/736x/ff/d8/10/ffd8109392e5aa39b56f341f4a388ee9.jpg',
  //   },
  //   {
  //     id: '34',
  //     quantity: 7,
  //     uri: 'https://i.pinimg.com/736x/5d/7f/5f/5d7f5f33f763c18b03fc6cd9836a423d.jpg',
  //   },
  //   {
  //     id: '35',
  //     quantity: 8,
  //     uri: 'https://i.pinimg.com/736x/c6/12/8a/c6128ae7a90bed67e450fa6376891273.jpg',
  //   },
  //   {
  //     id: '36',
  //     quantity: 9,
  //     uri: 'https://i.pinimg.com/736x/b7/c2/31/b7c2314472307131946d9b255c3c06f7.jpg',
  //   },
  //   {
  //     id: '37',
  //     quantity: 10,
  //     uri: 'https://i.pinimg.com/736x/2d/e2/ca/2de2caefc8094a183aaa3a070e9ed420.jpg',
  //   },
  //   {
  //     id: '38',
  //     quantity: 1,
  //     uri: 'https://i.pinimg.com/736x/ff/d8/10/ffd8109392e5aa39b56f341f4a388ee9.jpg',
  //   },
  //   {
  //     id: '39',
  //     quantity: 2,
  //     uri: 'https://i.pinimg.com/736x/5d/7f/5f/5d7f5f33f763c18b03fc6cd9836a423d.jpg',
  //   },
  //   {
  //     id: '40',
  //     quantity: 3,
  //     uri: 'https://i.pinimg.com/736x/c6/12/8a/c6128ae7a90bed67e450fa6376891273.jpg',
  //   },
  //   {
  //     id: '41',
  //     quantity: 4,
  //     uri: 'https://i.pinimg.com/736x/b7/c2/31/b7c2314472307131946d9b255c3c06f7.jpg',
  //   },
  //   {
  //     id: '42',
  //     quantity: 5,
  //     uri: 'https://i.pinimg.com/736x/2d/e2/ca/2de2caefc8094a183aaa3a070e9ed420.jpg',
  //   },
  //   {
  //     id: '43',
  //     quantity: 6,
  //     uri: 'https://i.pinimg.com/736x/ff/d8/10/ffd8109392e5aa39b56f341f4a388ee9.jpg',
  //   },
  //   {
  //     id: '44',
  //     quantity: 7,
  //     uri: 'https://i.pinimg.com/736x/5d/7f/5f/5d7f5f33f763c18b03fc6cd9836a423d.jpg',
  //   },
  //   {
  //     id: '45',
  //     quantity: 8,
  //     uri: 'https://i.pinimg.com/736x/c6/12/8a/c6128ae7a90bed67e450fa6376891273.jpg',
  //   },
  //   {
  //     id: '46',
  //     quantity: 9,
  //     uri: 'https://i.pinimg.com/736x/b7/c2/31/b7c2314472307131946d9b255c3c06f7.jpg',
  //   },
  //   {
  //     id: '47',
  //     quantity: 10,
  //     uri: 'https://i.pinimg.com/736x/2d/e2/ca/2de2caefc8094a183aaa3a070e9ed420.jpg',
  //   },
  //   {
  //     id: '48',
  //     quantity: 1,
  //     uri: 'https://i.pinimg.com/736x/ff/d8/10/ffd8109392e5aa39b56f341f4a388ee9.jpg',
  //   },
  //   {
  //     id: '49',
  //     quantity: 2,
  //     uri: 'https://i.pinimg.com/736x/5d/7f/5f/5d7f5f33f763c18b03fc6cd9836a423d.jpg',
  //   },
  //   {
  //     id: '50',
  //     quantity: 3,
  //     uri: 'https://i.pinimg.com/736x/c6/12/8a/c6128ae7a90bed67e450fa6376891273.jpg',
  //   },
  // ];

  const { tokenContext, idContext, avatarContext, usernameContext } = useContext(AuthContext);

  const [loading, setLoading] = useState(true);
  const [username, setUsername] = useState('');
  const [numPost, setNumPost] = useState(0);
  const [posts, setPosts] = useState([]);
  const [medias, setMedias] = useState([]);
  const [avatar, setAvatar] = useState();
  const [userState, setUserState] = useState(user);
  const [numFollowing, setNumFollowing] = useState(0);
  const [numFollower, setNumFollower] = useState(0);
  const [userSuggestion, setUserSuggestion] = useState(null);
  const [isOpenQR, setIsOpenQR] = useState(false);
  const navigation = useNavigation();
  const [isFollow, setIsFollow] = useState(false);
  const route = useRoute();
  const [commentsByPost, setCommentsByPost] = useState({});
  const [commentInputs, setCommentInputs] = useState({});
  const [editingCommentId, setEditingCommentId] = useState(null);
  const [editingCommentText, setEditingCommentText] = useState('');
  const [isModalVisible, setModalVisible] = useState(false);
  const [isModalEditVisible, setModalEditVisible] = useState(false);
  const [selectedPostId, setSelectedPostId] = useState(null);
  const [newCaption, setNewCaption] = useState('');
  const isFocused = useIsFocused();

  const fetchData = async () => {
    if (!tokenContext) {
      console.log("Profile: No token, skipping fetch");
      return;
    }
    
    try {
      setLoading(true);
      let currentUsername = '';
      
      if (route.params?.userId) {
        const userId = route.params.userId;
        const endpoint = `${ENDPOINTS.USER.GET_USER_PROFILE}/${userId}`;
        const response = await axios.get(endpoint, {
          headers: { Authorization: `Bearer ${tokenContext}` },
        });
        if (response.status === 200) {
          const userData = response.data.result;
          setUserState(userData);
          currentUsername = userData.username;
          setUsername(currentUsername);
          setAvatar(userData.avatar);
        }
      } else {
        if (isUser) {
          currentUsername = usernameContext || '';
          if (currentUsername) {
            setUsername(currentUsername);
            setAvatar(avatarContext);
          }
        } else {
          currentUsername = userState?.username || '';
          if (currentUsername) {
            setUsername(currentUsername);
            setAvatar(userState?.avatar);
          }
        }
      }
      
      console.log("Profile: Fetching data for username:", currentUsername);
      
      if (currentUsername && currentUsername.length !== 0) {
        await Promise.all([
          fetchPost(currentUsername), 
          fetchFollower(currentUsername), 
          !isUser && fetchIsFollow(), 
          fetchFollowing(currentUsername), 
          fetchSuggestUser()
        ]);
      } else {
        console.log("Profile: No username available, cannot fetch data");
      }
      setLoading(false);
    } catch (error) {
      console.error("Error fetching data: ", error);
      setLoading(false);
    }
  };

  // Reload when screen is focused
  useEffect(() => {
    if (isFocused && tokenContext) {
      console.log("Profile: Screen focused, reloading data");
      fetchData();
    }
  }, [isFocused]);

  // Initial load and when dependencies change
  useEffect(() => {
    if (tokenContext) {
      // Small delay to ensure context is ready
      const timer = setTimeout(() => {
        console.log("Profile: Initial load, tokenContext:", !!tokenContext, "usernameContext:", usernameContext);
        fetchData();
      }, 100);
      return () => clearTimeout(timer);
    }
  }, [tokenContext, route.params?.userId, isUser, usernameContext]);
  async function fetchPost(usernameParam = null) {
    const endpoint = ENDPOINTS.USER.GET_POST_BY_USERNAME;
    const targetUsername = usernameParam || username;
    if (!targetUsername) {
      console.log("Profile: fetchPost - No username provided");
      return;
    }
    
    try {
      console.log("Profile: fetchPost - Fetching posts for:", targetUsername);
      const response = await axios.post(
        endpoint,
        { username: targetUsername },
        {
          headers: { Authorization: `Bearer ${tokenContext}` },
        }
      );
      const { result } = response.data || {};
      
      // API cũ trả về PostResponseWithoutUser có userId (UUID), không có user object
      // Cần tạo user object từ thông tin đã có
      const visiblePosts = Array.isArray(result) 
        ? result
          .filter(post => {
            // Chỉ lấy posts hợp lệ có id và userId
            return post && post.id && post.userId;
          })
          .filter(post => post?.visible !== false)
          .map(post => {
            // Tạo user object từ thông tin đã có
            // Ưu tiên: userState?.avatar > avatarContext > avatar state
            const userAvatar = userState?.avatar || avatarContext || avatar || null;
            console.log("Profile: fetchPost - Creating user object with avatar:", userAvatar);
            return {
              ...post,
              user: {
                id: post.userId,
                username: targetUsername, // Dùng username đang fetch
                avatar: userAvatar, // Dùng avatar từ userState hoặc context
              }
            };
          })
          .sort((a, b) => {
            const dateA = a.createdAt ? new Date(a.createdAt) : new Date(0);
            const dateB = b.createdAt ? new Date(b.createdAt) : new Date(0);
            return dateB - dateA;
          })
        : [];
      
      console.log("Profile: fetchPost - Found", visiblePosts.length, "posts");
      setNumPost(visiblePosts.length);
      
      // Fetch media for posts từ Cloudinary (giống Home.jsx)
      if (visiblePosts.length > 0) {
        // Set posts trước
        setPosts(visiblePosts);
        
        // Lấy media dựa trên các post đã lấy (dùng Cloudinary như Home.jsx)
        let folders = visiblePosts.map(value => `posts/${value.userId || value.user?.id}/${value.id}`);
        console.log("Profile: fetchPost - Folders for Cloudinary:", folders);
        
        const mediasResponse = await fetchMediaFromCloudinary(folders);
        console.log("Profile: fetchPost - Media response:", mediasResponse);
        setMedias(mediasResponse || []);
        
        await fetchCommentsForPosts(visiblePosts);
      } else {
        setMedias([]);
        setPosts([]);
      }
    } catch (error) {
      console.log("Error fetching posts:", error);
      handleError(error);
      setPosts([]);
      setMedias([]);
    }
  }

  const fetchMediaFromCloudinary = async (folders) => {
    const multipleMediaEndpoint = ENDPOINTS.CLOUDINARY.FIND_ALL_MULTIPLE;
    if (!folders || folders.length === 0) return [];
    try {
      const mediaResponse = await axios.post(multipleMediaEndpoint, folders, {
        headers: { Authorization: `Bearer ${tokenContext}` },
      });
      return mediaResponse?.data?.result || [];
    } catch (error) {
      console.log(`Profile: Lỗi khi gọi API: ${multipleMediaEndpoint}`, error);
      handleError(error);
      return [];
    }
  };

  const fetchMediaFromDatabase = async (posts) => {
    const mediaEndpoint = ENDPOINTS.MEDIA.FIND_ALL_MULTIPLE_POST;
    if (!posts || posts.length === 0) {
      setMedias([]);
      return;
    }
    try {
      console.log("Profile: fetchMediaFromDatabase - Fetching for posts:", posts.length);
      // Convert posts to format expected by API (cần id và user)
      const postsForAPI = posts.map(post => ({
        id: post.id,
        user: post.user ? { id: post.user.id } : null
      }));
      
      const mediaResponse = await axios.post(mediaEndpoint, postsForAPI, {
        headers: { Authorization: `Bearer ${tokenContext}` },
      });
      const mediaResult = mediaResponse?.data?.result || [];
      console.log("Profile: fetchMediaFromDatabase - Received", mediaResult.length, "media arrays");
      
      // Convert List<List<Media>> to List<List<String>> (chỉ lấy mediaUrl)
      const mediaUrls = mediaResult.map((mediaList) => {
        if (!Array.isArray(mediaList)) {
          console.log("Profile: fetchMediaFromDatabase - mediaList is not array:", mediaList);
          return [];
        }
        return mediaList.map(media => {
          const url = media?.mediaUrl || media?.media_url || media?.url || media;
          console.log("Profile: fetchMediaFromDatabase - Extracted URL:", url, "from media:", JSON.stringify(media));
          return url;
        }).filter(url => url && typeof url === 'string');
      });
      
      console.log("Profile: fetchMediaFromDatabase - Media URLs:", JSON.stringify(mediaUrls, null, 2));
      console.log("Profile: fetchMediaFromDatabase - First media URL sample:", mediaUrls[0]?.[0]);
      setMedias(mediaUrls);
    } catch (error) {
      console.log("Media error", error);
      console.log("Media error details:", error.response?.data);
      setMedias([]);
    }
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
      try { handleError(error); } catch (e) {}
    }
  };

  const fetchFollower = async (usernameParam = null) => {
    const endpoint = ENDPOINTS.FOLLOW.FOLLOWER;
    const targetUsername = usernameParam || username;
    if (!targetUsername) return;
    
    try {
      const response = await axios.post(
        endpoint,
        { username: targetUsername }
      )
      if (response.data.code === 200) {
        setNumFollower(response.data.result);
      }
    } catch (error) {
      console.log("Follower error: ", error);
    }
  }

  const fetchFollowing = async (usernameParam = null) => {
    const endpoint = ENDPOINTS.FOLLOW.FOLOWERING
    const targetUsername = usernameParam || username;
    if (!targetUsername) return;
    
    try {
      const response = await axios.post(
        endpoint,
        { username: targetUsername }
      )
      if (response.data.code === 200) {
        setNumFollowing(response.data.result);
      }
    } catch (error) {
      console.log("Following error: ", error);
    }
  };

  const fetchIsFollow = async () => {
    const endpoint = ENDPOINTS.FOLLOW.IS_FOLLOW;
    try {
      const response = await axios.post(endpoint,
        {
          followerId: idContext,
          followingId: userState.id
        }
      );
      setIsFollow(response.data)
    } catch (error) {
      console.log("IsFollow error: ", error)
    }
  };

  const fetchSuggestUser = async () => {
    const endpoint = ENDPOINTS.FOLLOW.SUGGEST_USER;
    try {
      const response = await axios.post(endpoint,
        { username: usernameContext }
      );

      setUserSuggestion(response.data);
    } catch (error) {
      console.log("Suggest error", error)
    }
  }


  const handleFollow = async (followingId) => {
    const endpoint = ENDPOINTS.FOLLOW.FOLLOW;
    try {
      const response = await axios.post(endpoint,
        {
          followerId: idContext,
          followingId: followingId
        }
      );
      setLoading(true);
    } catch (error) {
      console.log("Follow error ", error);
    }
  }

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
      try { handleError(error); } catch (e) { }
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

  const toggleModal = (postId, caption) => {
    setSelectedPostId(postId);
    setNewCaption(caption);
    setModalVisible(!isModalVisible);
  };

  const toggleModalEditPost = () => {
    setModalEditVisible(!isModalEditVisible);
  };

  const handleEdit = async () => {
    if (!newCaption.trim()) {
      toggleModal();
      toggleModalEditPost();
      return;
    }
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
    await fetchPost(username);
    toggleModal();
    toggleModalEditPost();
  };

  const handleDelete = async () => {
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
    await fetchPost(username);
    toggleModal();
  };

  const renderItem = ({ item: url, index: idxChild }) => {
    if (!url || typeof url !== 'string') {
      console.log("Profile: renderItem - Invalid URL:", url, typeof url);
      return null;
    }
    
    console.log("Profile: renderItem - Rendering URL:", url);
    const lowerUrl = url.toLowerCase();
    
    // Check if it's a video
    const isVideo = lowerUrl.endsWith('.mp4') || lowerUrl.endsWith('.mov') || lowerUrl.includes('video');
    
    if (isVideo) {
      return (
        <Video
          key={idxChild}
          style={styles.selectedVideo}
          source={{ uri: url }}
          controls={true}
          resizeMode="contain"
          onError={(error) => {
            console.log("Profile: Video load error:", error);
          }}
        />
      );
    } else {
      // Default to image for everything else (jpg, png, jpeg, webp, or any URL)
      return (
        <Image
          key={idxChild}
          source={{ uri: url }}
          style={styles.selectedImage}
          resizeMode="cover"
          onError={(error) => {
            console.log("Profile: Image load error:", error.nativeEvent?.error || error);
            console.log("Profile: Failed URL:", url);
          }}
          onLoad={() => {
            console.log("Profile: Image loaded successfully:", url);
          }}
        />
      );
    }
  };

  if (loading) {
    return (
      <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
        <ActivityIndicator size="large" color="#0000ff" />
      </View>
    );
  } else {
    return (
      <View style={styles.screen}>
        <ScrollView
          className="bg-white"
          horizontal={false}
          showsVerticalScrollIndicator={false}>
          <View className="w-full px-4">
            {/* Profile Header Section */}
            <View style={styles.profileSection}>
              <View style={styles.profileHeader}>
                {/* Avatar */}
                <View style={styles.avatarContainer}>
                  {!avatar ? (
                    <Image
                      source={require('../../assets/avatarDefine.jpg')}
                      style={styles.avatar}
                    />
                  ) : (
                    <Image
                      source={{ uri: avatar }}
                      style={styles.avatar}
                    />
                  )}
                </View>
                
                {/* Stats */}
                <View style={styles.statsContainer}>
                  <View style={styles.stat}>
                    <Text style={styles.statNumber}>{numPost}</Text>
                    <Text style={styles.statLabel}>posts</Text>
                  </View>
                  <View style={styles.stat}>
                    <Text style={styles.statNumber}>{numFollower}</Text>
                    <Text style={styles.statLabel}>followers</Text>
                  </View>
                  <View style={styles.stat}>
                    <Text style={styles.statNumber}>{numFollowing}</Text>
                    <Text style={styles.statLabel}>following</Text>
                  </View>
                </View>
              </View>
              
              {/* Username */}
              <View style={styles.usernameContainer}>
                <Text style={styles.username}>{username}</Text>
              </View>
            </View>

            {/* Action Buttons */}
            {isUser ? (
              <View style={styles.actionButtonsContainer}>
                <Pressable
                  style={[styles.actionButton, { marginRight: 8 }]}
                  onPress={() => navigation.navigate('EditProfile')}>
                  <Text style={styles.actionButtonText}>Edit profile</Text>
                </Pressable>
                <Pressable 
                  style={[styles.actionButton, { marginRight: 8 }]}
                  onPress={() => { setIsOpenQR(true) }}>
                  <Text style={styles.actionButtonText}>Share profile</Text>
                </Pressable>
                <Pressable style={styles.iconButton}>
                  <FontAwesome6 name="user-plus" size={20} color="#111" />
                </Pressable>
              </View>
            ) : (
              <View style={styles.followButtonContainer}>
                {isFollow ? (
                  <Pressable 
                    style={styles.unfollowButton}
                    onPress={() => handleFollow(userState.id)}>
                    <Text style={styles.unfollowButtonText}>Unfollow</Text>
                  </Pressable>
                ) : (
                  <Pressable 
                    style={styles.followButton}
                    onPress={() => handleFollow(userState.id)}>
                    <Text style={styles.followButtonText}>Follow</Text>
                  </Pressable>
                )}
              </View>
            )}

            {/* Discover People Section */}
            {userSuggestion && userSuggestion.length > 0 && (
              <View style={styles.discoverSection}>
                <View style={styles.discoverHeader}>
                  <Text style={styles.discoverTitle}>Discover people</Text>
                  <Pressable>
                    <Text style={styles.seeAllText}>See all</Text>
                  </Pressable>
                </View>
                <ScrollView
                  horizontal={true}
                  showsHorizontalScrollIndicator={false}
                  contentContainerStyle={styles.suggestionScrollContainer}>
                  {userSuggestion.map((user, index) => (
                    <UserSuggestion 
                      key={index} 
                      follow={() => handleFollow(user.id)} 
                      username={user.username} 
                      caption="Suggested for you" 
                      image={user.avatar} 
                      id={user.id} 
                    />
                  ))}
                </ScrollView>
              </View>
            )}

            {/* Posts Section */}
            {posts.length > 0 ? (
              posts
                .filter(post => post && post.id && post.user) // Chỉ render posts hợp lệ
                .map((post, index) => {
                  const postUser = post.user;
                  // Dùng index trực tiếp như Home.jsx
                  const postMedias = medias?.[index] && Array.isArray(medias[index]) 
                    ? medias[index].slice().reverse() 
                    : [];
                  
                  console.log(`Profile: Post ${post.id} at index ${index}`);
                  console.log(`Profile: medias array length:`, medias?.length);
                  console.log(`Profile: medias[${index}]:`, medias?.[index]);
                  console.log(`Profile: postMedias length:`, postMedias.length);
                  
                  return (
                  <View style={styles.postContainer} key={post.id}>
                    {/* Header post */}
                    <View style={styles.postHeader}>
                      <TouchableOpacity style={styles.postHeaderLeft}>
                        <AvatarComponent user={postUser} size={40} />
                        <View style={styles.postUserInfo}>
                          <View style={styles.postUsernameRow}>
                            <Text style={styles.postUsername}>{postUser.username || username}</Text>
                            <Image
                              source={images.icon_verify}
                              style={{ width: 20, height: 20, marginLeft: 4 }}
                              resizeMode="contain"
                            />
                          </View>
                        </View>
                      </TouchableOpacity>

                      {/* Header right: chỉ cho chủ post được mở menu sửa/xóa */}
                      {isUser && idContext && postUser.id && String(postUser.id).toLowerCase() === String(idContext).toLowerCase() && (
                        <TouchableOpacity
                          onPress={() => toggleModal(post.id, post.caption || '')}
                          style={styles.optionsButton}>
                          <Image
                            source={images.icon_triple_dot}
                            style={{ width: 24, height: 24 }}
                          />
                        </TouchableOpacity>
                      )}
                    </View>

                  {/* Media - chỉ render khi có media URLs */}
                  {postMedias.length > 0 && (
                    <View style={styles.mediaWrapper}>
                      <View style={styles.mediaContainer}>
                        <FlatList
                          data={postMedias}
                          renderItem={renderItem}
                          keyExtractor={(item, idxChild) => `${post.id}-${idxChild}`}
                          horizontal
                          inverted
                          pagingEnabled
                          bounces={false}
                          showsHorizontalScrollIndicator={false}
                        />
                      </View>
                    </View>
                  )}

                  {/* Footer post */}
                  <View style={styles.postFooter}>
                    <View style={styles.postActions}>
                      <View style={{ marginRight: 16 }}>
                        <LikeButton postId={post.id} />
                      </View>
                      <TouchableOpacity style={{ marginRight: 16 }}>
                        <Image
                          source={images.icon_message}
                          style={{ width: 25, height: 25, transform: [{ scaleX: -1 }] }}
                        />
                      </TouchableOpacity>
                      <TouchableOpacity>
                        <Image
                          source={images.icon_share}
                          style={{ width: 25, height: 25 }}
                        />
                      </TouchableOpacity>
                    </View>

                    {/* Caption */}
                    {post.caption && (
                      <Text style={styles.postCaption}>{post.caption}</Text>
                    )}

                    {/* Comment Input */}
                    <View style={styles.commentInputRow}>
                      <View style={styles.commentAvatarPlaceholder}>
                        {avatar ? (
                          <Image
                            source={{ uri: avatar }}
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
                        <Text style={{ color: '#0095f6', fontWeight: '600' }}>Gửi</Text>
                      </TouchableOpacity>
                    </View>

                    {/* Comments List */}
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
                  </View>
                </View>
                );
              })
            ) : (
              <View style={styles.emptyPosts}>
                <Text style={styles.emptyPostsText}>Chưa có bài đăng nào</Text>
              </View>
            )}

            {/* Modals */}
            <RNModal
              isVisible={isModalVisible}
              onBackdropPress={() => setModalVisible(false)}
              backdropOpacity={0.1}
              style={styles.modal}>
              <View style={styles.modalContent}>
                <TouchableOpacity
                  onPress={() => toggleModalEditPost()}
                  style={styles.option}>
                  <Ionicons name="pencil-outline" size={20} color="#111827" />
                  <Text style={styles.optionText}>Edit</Text>
                </TouchableOpacity>
                <TouchableOpacity
                  onPress={() => handleDelete()}
                  style={styles.option}>
                  <Ionicons name="trash-outline" size={20} color="red" />
                  <Text style={[styles.optionText, { color: 'red' }]}>Delete</Text>
                </TouchableOpacity>
              </View>
            </RNModal>

            <RNModal
              isVisible={isModalEditVisible}
              onBackdropPress={() => setModalEditVisible(false)}
              backdropOpacity={0.1}
              style={styles.modal}>
              <View style={styles.modalEditContent}>
                <View style={styles.modalEditHeader}>
                  <TouchableOpacity onPress={() => setModalEditVisible(false)}>
                    <Ionicons name="arrow-back-outline" size={25} color="#111827" />
                  </TouchableOpacity>
                  <Text style={styles.modalEditTitle}>Sửa đổi</Text>
                  <TouchableOpacity onPress={() => handleEdit()}>
                    <Ionicons name="checkmark-outline" size={25} color="#111827" />
                  </TouchableOpacity>
                </View>
                <TextInput
                  style={styles.captionInput}
                  placeholder="Write a caption..."
                  placeholderTextColor="#9ca3af"
                  onChangeText={text => setNewCaption(text)}
                  value={newCaption}
                  multiline
                />
              </View>
            </RNModal>
          </View>
          <View className="pb-20"></View>
        </ScrollView>

        {/* QR Code Modal */}
        <Modal visible={isOpenQR} transparent={true} animationType="slide">
          <View style={styles.modalContainer}>
            <View style={styles.qrContainer}>
              <QRCode
                value={`https://myapp_instagram.com/profile/${idContext}`}
                size={200}
              />
              <TouchableOpacity
                style={styles.closeButton}
                onPress={() => setIsOpenQR(!isOpenQR)}>
                <Text style={styles.closeButtonText}>Đóng</Text>
              </TouchableOpacity>
            </View>
          </View>
        </Modal>

      </View>
    );
  }
};

const { width: SCREEN_WIDTH } = Dimensions.get('window');

const styles = StyleSheet.create({
  screen: {
    flex: 1,
    backgroundColor: '#fff',
  },
  container: {
    flex: 1,
    padding: 10,
    backgroundColor: '#fff',
  },
  profileSection: {
    paddingVertical: 16,
    marginBottom: 12,
  },
  profileHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 12,
  },
  avatarContainer: {
    marginRight: 20,
  },
  avatar: {
    width: 90,
    height: 90,
    borderRadius: 45,
    borderWidth: 2,
    borderColor: '#e5e7eb',
  },
  statsContainer: {
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'space-around',
    alignItems: 'center',
  },
  stat: {
    alignItems: 'center',
  },
  statNumber: {
    fontSize: 18,
    fontWeight: '700',
    color: '#111',
    marginBottom: 2,
  },
  statLabel: {
    fontSize: 13,
    color: '#6b7280',
    fontWeight: '400',
  },
  usernameContainer: {
    marginTop: 4,
  },
  username: {
    fontSize: 16,
    fontWeight: '600',
    color: '#111',
  },
  actionButtonsContainer: {
    flexDirection: 'row',
    marginBottom: 20,
  },
  actionButton: {
    flex: 1,
    backgroundColor: '#f3f4f6',
    paddingVertical: 8,
    paddingHorizontal: 16,
    borderRadius: 8,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: '#e5e7eb',
  },
  actionButtonText: {
    fontSize: 14,
    fontWeight: '600',
    color: '#111',
  },
  iconButton: {
    width: 40,
    height: 40,
    backgroundColor: '#f3f4f6',
    borderRadius: 8,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: '#e5e7eb',
  },
  followButtonContainer: {
    marginBottom: 20,
  },
  followButton: {
    backgroundColor: '#0095f6',
    paddingVertical: 10,
    paddingHorizontal: 24,
    borderRadius: 8,
    alignItems: 'center',
    justifyContent: 'center',
  },
  followButtonText: {
    fontSize: 14,
    fontWeight: '600',
    color: '#fff',
  },
  unfollowButton: {
    backgroundColor: '#f3f4f6',
    paddingVertical: 10,
    paddingHorizontal: 24,
    borderRadius: 8,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: '#e5e7eb',
  },
  unfollowButtonText: {
    fontSize: 14,
    fontWeight: '600',
    color: '#111',
  },
  discoverSection: {
    marginBottom: 24,
    paddingTop: 16,
    borderTopWidth: 1,
    borderTopColor: '#f3f4f6',
  },
  discoverHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
    paddingHorizontal: 4,
  },
  discoverTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#111',
  },
  seeAllText: {
    fontSize: 14,
    fontWeight: '500',
    color: '#0095f6',
  },
  suggestionScrollContainer: {
    paddingRight: 16,
  },
  postContainer: {
    marginBottom: 24,
    borderTopWidth: 1,
    borderTopColor: '#f3f4f6',
    paddingTop: 16,
    width: '100%',
  },
  postHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 12,
    marginBottom: 12,
  },
  postHeaderLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
  },
  postUserInfo: {
    marginLeft: 12,
  },
  postUsernameRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  postUsername: {
    fontSize: 16,
    fontWeight: '600',
    color: '#111827',
  },
  mediaWrapper: {
    width: SCREEN_WIDTH,
    marginLeft: -16, // Compensate for parent px-4 padding
    marginRight: -16,
    marginBottom: 5,
  },
  mediaContainer: {
    width: '100%',
    aspectRatio: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#fafafa',
    overflow: 'hidden',
  },
  selectedImage: {
    width: '100%',
    height: '100%',
    resizeMode: 'cover',
  },
  selectedVideo: {
    width: '100%',
    height: '100%',
  },
  mediaPlaceholder: {
    width: '100%',
    height: '100%',
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#fafafa',
  },
  mediaPlaceholderText: {
    color: '#9ca3af',
    fontSize: 14,
  },
  postFooter: {
    paddingHorizontal: 12,
  },
  postActions: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 8,
  },
  postCaption: {
    fontSize: 14,
    color: '#111827',
    marginBottom: 8,
    paddingHorizontal: 4,
  },
  commentInputRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 4,
    paddingVertical: 8,
    marginTop: 4,
  },
  commentAvatarPlaceholder: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: '#e5e7eb',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 8,
  },
  commentAvatarText: {
    fontWeight: '700',
    color: '#6b7280',
    fontSize: 14,
  },
  commentInput: {
    flex: 1,
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 20,
    backgroundColor: '#f3f4f6',
    fontSize: 14,
    color: '#111827',
    marginRight: 8,
  },
  sendButton: {
    paddingHorizontal: 8,
    paddingVertical: 6,
  },
  commentList: {
    paddingHorizontal: 4,
    paddingTop: 8,
    marginTop: 4,
  },
  commentItem: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    marginBottom: 8,
  },
  commentAvatar: {
    width: 28,
    height: 28,
    borderRadius: 14,
    backgroundColor: '#e5e7eb',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 8,
  },
  commentContent: {
    flex: 1,
  },
  commentBubble: {
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 18,
    backgroundColor: '#f3f4f6',
  },
  commentBubbleRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
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
    marginLeft: 8,
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
  emptyPosts: {
    paddingVertical: 40,
    alignItems: 'center',
  },
  emptyPostsText: {
    fontSize: 16,
    color: '#6b7280',
  },
  emptyMedia: {
    width: '100%',
    height: 400,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#f3f4f6',
  },
  emptyMediaText: {
    fontSize: 14,
    color: '#6b7280',
  },
  optionsButton: {
    padding: 8,
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
  },
  modalEditContent: {
    backgroundColor: 'white',
    padding: 20,
    borderTopLeftRadius: 20,
    borderTopRightRadius: 20,
    minHeight: 300,
  },
  modalEditHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 16,
  },
  modalEditTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#111827',
  },
  captionInput: {
    fontSize: 16,
    color: '#111827',
    minHeight: 100,
    textAlignVertical: 'top',
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
  postsContainer: {
    display: 'flex',
    flexDirection: 'row', // Thêm flexDirection: 'row'
    flexWrap: 'wrap', // Cho phép các item xuống dòng nếu không đủ chỗ
    marginTop: 10,
    width: '105%',
    backgroundColor: 'black',
    marginHorizontal: -10,
  },
  postImage: {
    width: '33.3%',
    height: '33.3%',
    backgroundColor: '#ccc',
    aspectRatio: 1, // Tạo hình vuông
    borderWidth: 0.5,
  },

  btnEditProfile: {
    marginRight: 5,
    padding: 7,
    paddingHorizontal: 35,
    alignItems: 'center',
    borderRadius: 7,
    backgroundColor: '#ccc',
    marginTop: 10,
  },
  explore: {
    marginTop: 20,
    marginBottom: 20,
  },
  item: {
    width: '50%',
    alignItems: 'center',
  },
  itemSelected: {
    borderBottomWidth: 1,
  },
  modalContainer: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.8)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  qrContainer: {
    backgroundColor: '#fff',
    padding: 20,
    borderRadius: 10,
    alignItems: 'center',
  },
  closeButton: {
    marginTop: 20,
    padding: 10,
    backgroundColor: '#1a73e8',
    borderRadius: 5,
  },
  closeButtonText: {
    color: '#fff',
    fontSize: 16,
  },
  textPrimary: {
    color: '#111111',
  },
  linkText: {
    color: '#3b82f6',
  },
  textOnPrimary: {
    color: '#ffffff',
    fontWeight: '600',
  },
});

export default Profile;
