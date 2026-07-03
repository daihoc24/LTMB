import React, {useContext, useEffect, useState} from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  Image,
  TextInput,
  Alert,
  ActivityIndicator,
  Pressable,
  Keyboard,
  Dimensions,
} from 'react-native';
import axios from 'axios';
import images from './../config/images';
import ENDPOINTS from './../config/endpoints';
import {AuthContext} from '../context/AuthContext';

const SignIn = ({navigation, route}) => {
  console.log(
    `[SCREEN NAVIGATION] ${new Date().toISOString()} - Screen: SignIn`,
  );

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);
  
  // Get screen dimensions for responsive design
  const screenWidth = Dimensions.get('window').width;
  const screenHeight = Dimensions.get('window').height;
  // Use 85% of screen width for inputs, but max 400px for better UX on large screens
  const inputWidth = Math.min(screenWidth * 0.85, 400);

  const {
    tokenContext,
    setTokenContext,
    setIdContext,
    setUsernameContext,
    setEmailContext,
    setCreatedAtContext,
    setBirthdayContext,
    setRoleContext,
  } = useContext(AuthContext);

  const handleCheckFormat = () => {
    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

    if (email.trim() === '') {
      Alert.alert(
        'Error',
        'Email field cannot be left blank. Please enter your email.',
      );
      return false;
    }
    if (!emailRegex.test(email)) {
      Alert.alert(
        'Error',
        'The email you entered is not valid. Please provide a valid email address.',
      );
      return false;
    }

    return true;
  };

  const handleSignIn = async () => {
    setLoading(true);

    if (!handleCheckFormat()) {
      setLoading(false);
      return;
    }

    try {
      const endpoint = ENDPOINTS.AUTH.GET_TOKEN;
      console.log(`Instagram-SignIn-endpoint: ${endpoint}`);

      const signInRequest = {
        email: email,
        password: password,
      };

      console.log(`Request: ${signInRequest}`);

      const response = await axios.post(endpoint, signInRequest);

      const {code, message, result} = response.data;
      const statusCode = response.status; // HTTP Status Code từ server

      switch (statusCode) {
        case 200: {
          // Trường hợp đăng nhập thành công
          const {authenticated, token} = result;
          if (authenticated) {
            setTokenContext(token);
            console.log('Token:', token);
            navigation.replace('(tabs)');
          } else {
            Alert.alert('Error', 'Authentication failed. Please try again.');
          }
          break;
        }
        default: {
          // Trường hợp lỗi không xác định
          Alert.alert('Error', message || 'An unexpected error occurred.');
          break;
        }
      }
    } catch (error) {
      if (error.response) {
        // Trường hợp server trả về phản hồi với mã lỗi HTTP (4xx, 5xx)

        const {status, data, headers} = error.response;
        const {code, message} = data;

        switch (status) {
          case 400:
            if (code == 1040) Alert.alert('Error', message);
            else
              console.log(`It's not #1040\tCode ${code}, Message: ${message}`);
            break;
          case 401:
            if (code == 1040) Alert.alert(message);
            else
              console.log(`It's not #1040\tCode ${code}, Message: ${message}`);
            break;
          default:
            break;
        }

        console.error('SignIn Error: Server responded with an error', {
          status: status,
          data: data,
          headers: headers,
        });
      } else if (error.request) {
        // Trường hợp không nhận được phản hồi từ server (timeout, server không khả dụng, v.v.)
        console.error('SignIn Error: No response received from server', {
          request: error.request,
        });

        Alert.alert(
          'Network Error',
          'No response received from the server. Please check your connection and try again.',
        );
      } else {
        // Các lỗi khác xảy ra trước khi gửi request (cấu hình sai, lỗi mã, v.v.)
        console.error(
          'SignIn Error: An error occurred while setting up the request',
          {
            message: error.message,
          },
        );

        Alert.alert(
          'Unexpected Error',
          `An unexpected error occurred:\n${error.message}`,
        );
      }
    } finally {
      setLoading(false);
    }
  };

  const handleSignUp = () => {
    navigation.navigate('(auths)');
  };

  const handleFacebookLogin = () => {
    Alert.alert('', 'This feature will be updated later');
  };

  const handleTogglePassword = () => {
    setIsPasswordVisible(prevState => !prevState);
  };

  return (
    <View
      style={{
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: '#ffffff',
        paddingHorizontal: 16,
      }}>
      {/* Image */}
      <View
        style={{
          width: Math.min(screenWidth * 0.58, 240),
          marginBottom: Math.min(screenHeight * 0.04, 32),
        }}>
        <Image
          style={{width: '100%', height: 64}}
          source={images.logo_text}
          resizeMode="contain"
        />
      </View>

      {/* Email field */}
      <TextInput
        style={{
          borderWidth: 1,
          borderColor: '#e5e7eb',
          paddingVertical: 12,
          paddingHorizontal: 16,
          width: inputWidth,
          marginBottom: 12,
          borderRadius: 16,
          fontSize: 14,
          color: '#111827',
        }}
        onChangeText={setEmail}
        placeholder="Phone number, username or email address"
        placeholderTextColor="#9ca3af"
        value={email}
      />
      {/* Password field */}
      <View style={{width: inputWidth, position: 'relative', marginBottom: 12}}>
        <TextInput
          style={{
            borderWidth: 1,
            borderColor: '#e5e7eb',
            paddingVertical: 12,
            paddingHorizontal: 16,
            width: '100%',
            borderRadius: 16,
            fontSize: 14,
            paddingRight: 50,
            color: '#111827',
          }}
          onChangeText={setPassword}
          placeholder="Password"
          placeholderTextColor="#9ca3af"
          value={password}
          secureTextEntry={!isPasswordVisible}
        />
        <Pressable
          style={{
            position: 'absolute',
            right: 12,
            top: '50%',
            transform: [{translateY: -10}],
          }}
          onPress={handleTogglePassword}>
          <Image
            style={{height: 20}}
            source={isPasswordVisible ? images.icon_show : images.icon_hide}
            resizeMode="contain"
          />
        </Pressable>
      </View>

      {/* Forgot password redirect */}
      <TouchableOpacity
        style={{width: inputWidth, marginBottom: 12}}
        onPress={() => Alert.alert('Feature not implemented')}>
        <Text
          style={{
            color: '#2563eb',
            textAlign: 'right',
            fontWeight: '500',
            fontSize: 14,
          }}>
          Forgotten password?
        </Text>
      </TouchableOpacity>

      {/* Log in button */}
      <Pressable
        style={{
          width: inputWidth,
          backgroundColor: '#2563eb',
          paddingVertical: 14,
          borderRadius: 16,
        }}
        onPress={handleSignIn}
        disabled={loading}>
        {loading ? (
          <ActivityIndicator size="small" color="#ffffff" />
        ) : (
          <Text style={{textAlign: 'center', fontSize: 16, fontWeight: '500', color: '#ffffff'}}>
            Log in
          </Text>
        )}
      </Pressable>

      <View style={{position: 'relative', justifyContent: 'center', alignItems: 'center', marginVertical: 28}}>
        <View style={{width: inputWidth, backgroundColor: '#d1d5db', height: 0.5}} />
        <Text style={{position: 'absolute', backgroundColor: '#ffffff', paddingHorizontal: 8, fontSize: 14, color: '#6b7280'}}>Or</Text>
      </View>

      {/* Log in with Facebook account */}
      <TouchableOpacity
        style={{flexDirection: 'row', alignItems: 'center', justifyContent: 'center'}}
        onPress={handleFacebookLogin}>
        <Image style={{height: 24, marginRight: 8}} source={images.icon_fb} resizeMode="contain" />
        <Text style={{fontSize: 14, color: '#2563eb', fontWeight: '500'}}>
          Log in with Facebook
        </Text>
      </TouchableOpacity>

      {/* Sign up redirect */}
      <View style={{
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        paddingVertical: 24,
        borderTopWidth: 1,
        borderTopColor: '#d1d5db',
        width: '100%',
        position: 'absolute',
        bottom: 0,
      }}>
        <Text style={{fontSize: 14, color: '#6b7280'}}>Don't have an account?</Text>
        <Pressable style={{marginLeft: 8}} onPress={handleSignUp}>
          <Text style={{fontSize: 14, fontWeight: '500', color: '#2563eb'}}>Sign up</Text>
        </Pressable>
      </View>
    </View>
  );
};

export default SignIn;
