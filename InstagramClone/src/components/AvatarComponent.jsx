import { Image, StyleSheet, Text, View } from 'react-native'
import React, { useState, useEffect } from 'react'
import { Avatar } from 'react-native-paper'
import images from '../config/images';

// Helper function to resize Cloudinary images
const resizeCloudinaryImage = (url, width = 200, height = 200) => {
    if (!url || typeof url !== 'string') return url;
    
    // Check if it's a Cloudinary URL
    if (url.includes('res.cloudinary.com') && url.includes('/upload/')) {
        // Cloudinary URL format: .../upload/[TRANSFORMATIONS/]v123/filename.jpg
        // We want to insert transformation before version or filename
        const uploadIndex = url.indexOf('/upload/');
        const afterUpload = url.substring(uploadIndex + 8); // After '/upload/'
        
        // Check if there's already a transformation (contains w_, h_, c_, q_)
        if (!afterUpload.match(/[whcq]_/)) {
            // No transformation yet, add one
            const transform = `w_${width},h_${height},c_fill,q_auto,f_auto`;
            return url.substring(0, uploadIndex + 8) + transform + '/' + afterUpload;
        }
    }
    return url;
};

const AvatarComponent = ({ size, user }) => {
    const defaultAvatar = require('../assets/avatarDefine.jpg');
    const [avatarSource, setAvatarSource] = useState(defaultAvatar);
    const avatarSize = size || 70;

    // Update avatar when user.avatar changes
    useEffect(() => {
        if (user?.avatar && typeof user.avatar === 'string' && user.avatar.trim() !== '') {
            // Resize Cloudinary images to optimize loading
            const optimizedUrl = resizeCloudinaryImage(user.avatar, avatarSize * 2, avatarSize * 2);
            console.log("AvatarComponent: Setting avatar URL:", optimizedUrl);
            setAvatarSource({ uri: optimizedUrl });
        } else {
            console.log("AvatarComponent: Using default avatar, user.avatar:", user?.avatar);
            setAvatarSource(defaultAvatar);
        }
    }, [user?.avatar, avatarSize]);

    const handleError = (error) => {
        console.log("AvatarComponent: Image load error:", error);
        console.log("AvatarComponent: Falling back to default avatar");
        setAvatarSource(defaultAvatar); // Sử dụng ảnh mặc định khi có lỗi
    };

    const seen = false

    return (
        <View className="overflow-hidden flex flex-row justify-center items-center">
            <Avatar.Image
                className=""
                size={size || 70}
                source={avatarSource}
                onError={handleError} // Gán hàm xử lý lỗi
            />
            {/* Khung viền PNG (phía trên) */}
            <Image
                className="w-full h-full justify-center items-center absolute z-10 rounded-full" // Đặt trên cùng với z-10
                source={seen != true ? images.story_unseen : images.story_seen}
            />
        </View>
    )
}

export default AvatarComponent

const styles = StyleSheet.create({})