import React, { useState, useContext, useEffect, useLayoutEffect } from 'react';
import {
  View,
  Text,
  Button,
  Image,
  Alert,
  StyleSheet,
  TouchableOpacity,
  ScrollView,
  Pressable,
} from 'react-native';
import axios from 'axios';
import { launchImageLibrary } from 'react-native-image-picker';
import ENDPOINTS from '../../config/endpoints';
import { AuthContext } from '../../context/AuthContext';
import { Dialog, Portal, Switch, TextInput, useTheme } from 'react-native-paper';
import { Dropdown } from 'react-native-element-dropdown';
import MaterialIcons from 'react-native-vector-icons/MaterialIcons';
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons';
import { handleError } from '../../utils/handleError';

function EditProfile({ navigation }) {
  const theme = useTheme();
  // Khóa màu input về nền sáng để không bị phụ thuộc dark mode của máy
  const inputTheme = {
    ...theme,
    colors: {
      ...theme.colors,
      background: '#ffffff',
      surface: '#ffffff',
      onSurface: '#111111',
      primary: '#0095f6',
      outline: '#d4d4d4',
    },
  };
  // Context
  const {
    tokenContext,
    idContext,
    usernameContext,
    setUsernameContext,
    avatarContext,
    setAvatarContext,
    privacyContext,
    setPrivacyContext,
  } = useContext(AuthContext);

  // Field user information
  const [name, setName] = useState();
  const [username, setUsername] = useState();
  const [bio, setBio] = useState();

  const [privacy, setPrivacy] = useState(true);
  const [avatar, setAvatar] = useState('');
  // Other
  const [loading, setLoading] = useState(true);
  const [visibleDialog, setVisibleDialog] = useState(false);
  useEffect(() => {
    if (avatarContext) {
      setAvatar(avatarContext);
    }
  }, [avatarContext])
  const selectImage = () => {
    const options = {
      mediaType: 'photo',
      quality: 1,
    };

    launchImageLibrary(options, response => {
      if (response.didCancel) {
        console.log('User cancelled image picker');
      } else if (response.assets && response.assets.length > 0) {
        uploadImage(response.assets[0]);
      } else {
        console.error('ImagePicker Error: ', response.errorMessage);
      }
    });

  };

  const uploadImage = async (image) => {
    const formData = new FormData();
    formData.append('username', usernameContext); // Thay thế bằng username thực tế
    formData.append('file', {
      uri: image.uri,
      type: image.type || 'image/jpeg', // Đặt loại tệp tin mặc định
      name: image.fileName || 'photo.jpg', // Tên tệp tin mặc định
    });

    try {
      const endpoint = ENDPOINTS.USER.UPDATE_AVATAR;
      console.log(`updateAvatar: ${endpoint}`);
      console.log(`usernameContext: ${usernameContext}`);
      console.log(`Image info:`, {
        uri: image.uri,
        type: image.type,
        fileName: image.fileName,
        fileSize: image.fileSize,
      });

      // Không set thủ công Content-Type, để axios tự thêm boundary cho multipart
      const response = await axios.post(endpoint, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
        timeout: 30000, // 30 seconds timeout
      });

      console.log('Upload success:', {
        status: response.status,
        statusText: response.statusText,
        data: response.data,
      });

      Alert.alert(
        'Success',
        response.data?.message || 'Image uploaded successfully!',
      );

      // Backend trả về trực tiếp URL ảnh (String)
      const imageUrl =
        typeof response.data === 'string'
          ? response.data
          : response.data?.result || response.data;

      setAvatar(imageUrl);
      setAvatarContext(imageUrl);
    } catch (error) {
      // Log chi tiết lỗi để debug
      console.error('=== UPLOAD ERROR DETAILS ===');
      console.error('Error message:', error.message);
      console.error('Error code:', error.code);
      console.error('Request URL:', error.config?.url);
      console.error('Request method:', error.config?.method);
      
      if (error.response) {
        // Server trả về response nhưng có lỗi
        console.error('Response status:', error.response.status);
        console.error('Response status text:', error.response.statusText);
        console.error('Response data:', error.response.data);
        console.error('Response headers:', error.response.headers);
        
        const errorMessage = typeof error.response.data === 'string' 
          ? error.response.data 
          : error.response.data?.message || JSON.stringify(error.response.data);
        
        // Kiểm tra nếu là lỗi Firebase chưa được cấu hình
        if (error.response.status === 503 && errorMessage.includes('Firebase')) {
          Alert.alert(
            'Firebase Not Configured',
            'Firebase configuration file is missing. Please contact administrator or check backend logs for setup instructions.',
            [{ text: 'OK' }]
          );
        } else {
          Alert.alert(
            'Error', 
            `Failed to upload image: ${error.response.status}\n${errorMessage}`
          );
        }
      } else if (error.request) {
        // Request được gửi nhưng không nhận được response
        console.error('Request was made but no response received');
        console.error('Request config:', error.config);
        console.error('Request data:', error.request);
        
        Alert.alert(
          'Network Error', 
          'Cannot connect to server. Please check your connection and try again.'
        );
      } else {
        // Lỗi khi setup request
        console.error('Error setting up request:', error.message);
        Alert.alert('Error', `Failed to upload image: ${error.message}`);
      }
      console.error('=== END ERROR DETAILS ===');
    }
  };
  const handleChangeUsername = (text) => {
    setUsername(text);
  }
  const handleUpdate = async () => {
    const endpoint = ENDPOINTS.USER.UPDATE_USER_PROFILE;
    console.log(`updateUser: ${endpoint}`);
    console.log(`id ${idContext}\tusername ${username}\tprivacy ${privacy}`);
    try {
      const response = await axios.post(
        endpoint,
        {
          id: idContext,
          username: username,
          privacy: privacy,
        },
        { headers: { Authorization: `Bearer ${tokenContext}` } },
      );

      if (response.status === 200) {
        const { result } = response.data;

        setUsernameContext(result.username);
        setPrivacyContext(result.privacy);
        console.log(result.username);
        setUsername(result.username);
        setPrivacy(result.privacy);

        Alert.alert('Success', 'Your profile has been updated successfully!');
      }
    } catch (error) {
      handleError(error);
    }
  };

  const handleUploadAvatar = () => { };

  useEffect(() => {
    setLoading(true);

    if (!tokenContext) navigation.goBack();
    else {
      if (usernameContext) {
        console.log(`usernameContext: ${usernameContext}`);
        setUsername(usernameContext);
      }
      // privacyContext là boolean nên có thể là false (hợp lệ),
      // vì vậy không được check if(privacyContext)
      if (typeof privacyContext === 'boolean') {
        console.log(`privacyContext: ${privacyContext}`);
        setPrivacy(privacyContext);
      }
    }

    setLoading(false);
  }, [tokenContext]);

  return (
    <View style={{ backgroundColor: '#fff', flex: 1 }}>
      <View className="w-full flex flex-row justify-center items-center py-3 bg-white">
        <View className="w-full px-4 flex flex-row justify-between items-center">
          <Pressable className='flex flex-row items-center'
            onPress={() => navigation.goBack()}>
            <MaterialCommunityIcons name='arrow-left' size={30} color="#111" />
            <Text className='text-lg font-medium ml-4' style={{ color: '#111' }}>Edit profile</Text>
          </Pressable>
          <Pressable
            onPress={handleUpdate}>
            <MaterialIcons name="done" size={30 + 4} color='blue' />
          </Pressable>
        </View>
      </View>
      <ScrollView
        className="w-full bg-white py-4"
        horizontal={false}
        showsVerticalScrollIndicator={false}
        contentContainerStyle={{ paddingBottom: 40 }}>
        <View className="w-full px-4">
          <View style={styles.editAvatar}>
            {avatar.length === 0 ? (
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
            <TouchableOpacity
              // onPress={handleUploadAvatar}
              onPress={selectImage}>
              <Text style={{ color: '#0095f6', fontSize: 15, fontWeight: 500 }}>
                Chỉnh sửa ảnh hoặc avatar
              </Text>
            </TouchableOpacity>
          </View>

          <View>
            <TextInput
              className="mb-3"
              label="Name"
              value={name}
              onChangeText={text => setName(text)}
              theme={inputTheme}
              style={{ backgroundColor: '#ffffff' }}
              outlineColor="#d4d4d4"
              activeOutlineColor="#0095f6"
              mode="outlined"
            />

            <TextInput
              className="mb-3"
              label="Username"
              value={username}
              onChangeText={handleChangeUsername}
              theme={inputTheme}
              style={{ backgroundColor: '#ffffff' }}
              outlineColor="#d4d4d4"
              activeOutlineColor="#0095f6"
              mode="outlined"
            />

            <TextInput
              className="mb-3"
              label="Bio"
              value={bio}
              onChangeText={text => setBio(text)}
              theme={inputTheme}
              style={{ backgroundColor: '#ffffff' }}
              outlineColor="#d4d4d4"
              activeOutlineColor="#0095f6"
              mode="outlined"
            />

            <Text className="border-b border-b-neutral-400 text-lg font-normal mb-2" style={{ color: '#111' }}>
              Account privacy
            </Text>
            <View className="flex flex-row items-center mb-2">
              <Text className="w-1/2 text-base" style={{ color: '#111' }}>Private account</Text>
              <Switch
                className="w-1/2"
                value={privacy}
                onValueChange={() => setPrivacy(!privacy)}
                color={theme.colors.primary}
              />
            </View>
            <Text className="text-xs font-light mb-1" style={{ color: '#111' }}>
              When your account is public, your profile and posts can be seen by
              anyone, on or off Instagram, even if they don't have an Instagram
              account.
            </Text>
            <Text className="text-xs font-light mb-10" style={{ color: '#111' }}>
              When your account is private, only the followers you approve can see
              what you share, including your photos or videos on hashtag and
              location pages, and your followers and following lists. Certain info
              on your profile, like your profile picture and username, is visible
              to everyone on and off instagram.
            </Text>

            <Pressable className="w-full p-2 border-b border-b-neutral-400">
              <Text className="text-base text-sky-500" style={{ color: '#0ea5e9' }}>
                Switch to professinal account
              </Text>
            </Pressable>
          </View>
        </View>
      </ScrollView>
    </View>

  );
}

const styles = StyleSheet.create({
  editAvatar: {
    alignItems: 'center',
    marginBottom: 20,
  },
  avatar: {
    width: 100,
    height: 100,
    borderRadius: 60,
    marginBottom: 10,
    backgroundColor: 'black',
  },
});

export default EditProfile;
