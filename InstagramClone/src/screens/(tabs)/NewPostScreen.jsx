import React, { useContext, useEffect, useState } from 'react';
import {
  View,
  Text,
  Button,
  ScrollView,
  Image,
  StyleSheet,
  TouchableOpacity,
  TextInput,
  Alert,
  KeyboardAvoidingView,
  Dimensions,
  Platform,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import axios from 'axios';
import { launchCamera, launchImageLibrary } from 'react-native-image-picker';
import ENDPOINTS from '../../config/endpoints';
import Ionicons from 'react-native-vector-icons/Ionicons';
// import {VLCPlayer, VlCPlayerView} from 'react-native-vlc-media-player';
import { AuthContext } from '../../context/AuthContext';
import { useIsFocused, useNavigation } from '@react-navigation/native';
import Video from 'react-native-video';

const NewPostScreen = () => {
  const [media, setMedia] = useState([]);
  const [text, setText] = useState('');
  const { tokenContext } = useContext(AuthContext);
  const navigation = useNavigation();
  const isFocused = useIsFocused();
  const [loading, setLoading] = useState(false)
  
  // Get screen dimensions for responsive design
  const screenWidth = Dimensions.get('window').width;
  const screenHeight = Dimensions.get('window').height;
  // Use 90% of screen width for images, but max 350px for better UX
  const imageSize = Math.min(screenWidth * 0.9, 350);

  const fetchData = async () => {
    setMedia([])
    setText('')
  };

  useEffect(() => {
    if (isFocused) {
      fetchData(); // reset lại màn hình khi được chọn
    }
  }, [isFocused]);

  const pickMedia = async () => {
    const options = {
      mediaType: 'mixed',
      maxWidth: 800,
      maxHeight: 600,
      quality: 0.8,
      selectionLimit: 0,
    };

    launchImageLibrary(options, response => {
      if (response.didCancel) {
        console.log('User cancelled image picker');
      } else if (response.errorCode) {
        console.log('Image Picker Error: ', response.errorMessage);
      } else {
        const selectedMedia = response.assets.map(asset => ({
          uri: asset.uri,
          type: asset.type,
          fileName: asset.fileName,
          width: asset.width,
          height: asset.height,
        }));

        setMedia([...media, ...selectedMedia]);
      }
    });
  };

  const handleCreatePost = async () => {
    if (loading) {
      console.log('Đang xử lý, vui lòng đợi...');
      return;
    }

    if (!text && media.length === 0) {
      Alert.alert('Thông báo', 'Vui lòng nhập nội dung hoặc thêm ảnh/video');
      return;
    }

    setLoading(true);
    console.log('Bắt đầu đăng bài...');

    try {
      const userInfoEndpoint = ENDPOINTS.USER.MY_INFORMATION;
      const userInfoResponse = await axios.get(userInfoEndpoint, {
        headers: {Authorization: `Bearer ${tokenContext}`},
      });
      const user = userInfoResponse.data.result;
      const newPost = {
        caption: text,
        user: {
          id: user.id,
        },
      };

      const endpoint = ENDPOINTS.POST.ADD;
      const introspectResponse = await axios.post(ENDPOINTS.AUTH.INTROSPECT, {
        token: tokenContext,
      });

      if (
        introspectResponse.data.code === 200 &&
        introspectResponse.data.result.valid
      ) {
        console.log('prepare create new post');

        const responseCreateNewPost = await axios.post(endpoint, newPost, {
          headers: { Authorization: `Bearer ${tokenContext}` },
        });

        const postId = responseCreateNewPost.data.result.id;
        var newMedia = [];
        console.log('success create new post');

        if (media.length > 0) {
          var formData = new FormData();
          media.forEach(values => {
            formData.append('fileUpload', {
              uri: values.uri,
              type: values.type,
              name: values.fileName,
            });
            newMedia.push({
              mediaUrl: values.uri,
              post: {
                id: postId,
              },
            });
          });

          // thêm ảnh vào db
          await axios.post(ENDPOINTS.MEDIA.ADD, newMedia, {
            headers: { Authorization: `Bearer ${tokenContext}` },
          });
          console.log('add media successfully in db');

          // thêm ảnh vào cloudinary
          await axios.post(ENDPOINTS.CLOUDINARY.ADD_MULTIPLE, formData, {
            params: {
              userId: user.id,
              postId: postId,
            },
            headers: {
              'Content-Type': 'multipart/form-data',
              Authorization: `Bearer ${tokenContext}`,
            },
          });
        }

        Alert.alert('Thành công', 'Đăng bài thành công');
        setMedia([]);
        setText('');
        navigation.navigate('Home');
      } else {
        Alert.alert('Lỗi', 'Đăng bài thất bại: Token không hợp lệ');
      }
    } catch (error) {
      console.error('Lỗi khi đăng bài:', error);
      Alert.alert(
        'Lỗi',
        error.response?.data?.message || error.message || 'Có lỗi xảy ra khi đăng bài'
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <KeyboardAvoidingView
      className="flex-1 bg-white"
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}>

      <ScrollView
        horizontal={false}
        showsVerticalScrollIndicator={false}
        keyboardShouldPersistTaps="handled"
        nestedScrollEnabled={true}>
        <View style={styles.header}>
          <TouchableOpacity onPress={() => navigation.goBack()}>
            <Ionicons name="arrow-back" size={24} color="black" />
          </TouchableOpacity>
          <Text style={styles.title}>Tạo Bài Viết</Text>

          <TouchableOpacity
            style={[
              styles.postButton,
              ((!text && media.length === 0) || loading) && styles.postButtonDisabled
            ]}
            onPress={handleCreatePost}
            disabled={(!text && media.length === 0) || loading}
            activeOpacity={0.7}
            hitSlop={{top: 10, bottom: 10, left: 10, right: 10}}>
            {loading ? (
              <Text style={styles.postButtonText}>Đang xử lý...</Text>
            ) : (
              <Text style={styles.postButtonText}>Đăng</Text>
            )}
          </TouchableOpacity>
        </View>

        {media.length <= 0 ? (
          <View style={[styles.imageLayout, {height: imageSize}]}>
            <Ionicons name="images-outline" size={Math.min(screenWidth * 0.3, 120)} color="#9ca3af" />
          </View>
        ) : null}

        <ScrollView horizontal style={styles.imageScroll}>
          {media.map((values, index) =>
            values ? (
              values.type.includes('image') ? (
                <Image
                  key={index}
                  source={{ uri: values.uri }}
                  style={[styles.image, {width: imageSize, height: imageSize}]}
                />
              ) : values.type.includes('video') ? (
                <Video
                  key={index}
                  style={[styles.selectedVideo, {width: imageSize, height: imageSize}]}
                  source={{ uri: values.uri }}
                  controls={true}
                  resizeMode="contain"
                  onBuffer={this.onBuffer}
                  onError={this.videoError}
                />
              ) : null
            ) : null,
          )}
        </ScrollView>

        <TouchableOpacity
          style={styles.addButton}
          onPress={pickMedia}
          activeOpacity={0.7}
          hitSlop={{top: 10, bottom: 10, left: 10, right: 10}}>
          <Text style={styles.addButtonText}>Thêm</Text>
        </TouchableOpacity>

        <TextInput
          style={styles.textInput}
          placeholder="Bạn đang nghĩ gì?"
          placeholderTextColor="#9ca3af"
          multiline={true}
          value={text}
          onChangeText={value => setText(value)}
        />
      </ScrollView>
    </KeyboardAvoidingView>

  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    paddingHorizontal: 10,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 12,
    marginBottom: 16,
  },
  title: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#111827',
  },
  imageScroll: {
    flexDirection: 'row',
    marginTop: 10,
    marginBottom: 10,
  },
  image: {
    marginRight: 8,
    borderRadius: 8,
  },
  selectedVideo: {
    marginRight: 8,
    borderRadius: 8,
  },
  imageLayout: {
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'rgba(0, 0, 0, 0.05)',
    width: '100%',
    marginRight: 8,
    borderRadius: 8,
  },
  addButton: {
    backgroundColor: '#2563eb',
    paddingVertical: 12,
    paddingHorizontal: 24,
    borderRadius: 8,
    marginTop: 12,
    marginBottom: 8,
    alignSelf: 'center',
  },
  addButtonText: {
    color: '#ffffff',
    fontSize: 16,
    fontWeight: '600',
  },
  textInput: {
    borderColor: '#ddd',
    borderWidth: 1,
    minHeight: 120,
    maxHeight: 150,
    marginTop: 20,
    marginHorizontal: 16,
    padding: 12,
    borderRadius: 10,
    fontSize: 16,
    backgroundColor: '#f9f9f9',
    color: '#111827',
    textAlignVertical: 'top',
  },
  postButton: {
    backgroundColor: '#2563eb',
    paddingVertical: 8,
    paddingHorizontal: 16,
    borderRadius: 8,
  },
  postButtonDisabled: {
    backgroundColor: '#9ca3af',
  },
  postButtonText: {
    color: '#ffffff',
    fontSize: 14,
    fontWeight: '600',
  },
  imageLayoutIcon: {
    display: 'flex',
    justifyContent: 'center',
    width: 100,
    height: 100,
  },
});

export default NewPostScreen;
