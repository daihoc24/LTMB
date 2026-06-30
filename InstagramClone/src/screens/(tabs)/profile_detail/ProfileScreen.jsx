import React, {useMemo, useState} from 'react';
import {
  SafeAreaView,
  ScrollView,
  View,
  StyleSheet,
  Text,
  Modal,
  TouchableOpacity,
  useWindowDimensions,
} from 'react-native';
import QRCode from 'react-native-qrcode-svg';

const ProfileScreen = ({userId, username}) => {
  const [isQRVisible, setQRVisible] = useState(false);
  const {width} = useWindowDimensions();

  // Keep UI legible on tall Realme GT Neo 2 screens (1080x2400)
  const dynamicStyles = useMemo(() => {
    const horizontalPadding = Math.max(16, width * 0.04);
    const contentWidth = width - horizontalPadding * 2;
    return {
      container: {
        paddingHorizontal: horizontalPadding,
      },
      button: {
        width: contentWidth,
      },
    };
  }, [width]);

  const toggleQRModal = () => {
    setQRVisible(!isQRVisible);
  };
  return (
    <SafeAreaView style={styles.safeArea}>
      <ScrollView
        contentContainerStyle={[styles.container, dynamicStyles.container]}
        showsVerticalScrollIndicator={false}>
        <View style={styles.header}>
          <Text style={styles.username}>{username || 'Chưa có tên'}</Text>
        </View>

        <View style={styles.profileInfo}>
          <Text style={styles.name}>{username || 'Cập nhật hồ sơ'}</Text>
        </View>

        <View style={styles.statsContainer}>
          <View style={styles.statItem}>
            <Text style={styles.statNumber}>0</Text>
            <Text style={styles.statLabel}>bài viết</Text>
          </View>
          <View style={styles.statItem}>
            <Text style={styles.statNumber}>0</Text>
            <Text style={styles.statLabel}>người theo dõi</Text>
          </View>
          <View style={styles.statItem}>
            <Text style={styles.statNumber}>0</Text>
            <Text style={styles.statLabel}>đang theo dõi</Text>
          </View>
        </View>

        {/* Buttons Section */}
        <View style={styles.buttonsContainer}>
          <TouchableOpacity
            style={[styles.button, dynamicStyles.button]}
            onPress={toggleQRModal}>
            <Text style={styles.buttonText}>Chia sẻ trang cá nhân</Text>
          </TouchableOpacity>
        </View>

        {/* QR Code Modal */}
        <Modal visible={isQRVisible} transparent={true} animationType="slide">
          <View style={styles.modalContainer}>
            <View style={styles.qrContainer}>
              <QRCode
                value={`https://myapp_instagram.com/profile/${userId}`}
                size={200}
              />
              <TouchableOpacity
                style={styles.closeButton}
                onPress={toggleQRModal}>
                <Text style={styles.closeButtonText}>Đóng</Text>
              </TouchableOpacity>
            </View>
          </View>
        </Modal>
      </ScrollView>
    </SafeAreaView>
  );
};
const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#f5f7fb',
  },
  container: {
    flex: 1,
    backgroundColor: '#f5f7fb',
    paddingVertical: 24,
  },
  header: {
    marginBottom: 16,
  },
  username: {
    color: '#111',
    fontSize: 20,
    fontWeight: 'bold',
    textAlign: 'center',
  },
  profileInfo: {
    alignItems: 'center',
    marginBottom: 16,
  },
  name: {
    color: '#222',
    fontSize: 18,
    marginTop: 10,
  },
  statsContainer: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    marginBottom: 24,
  },
  statItem: {
    alignItems: 'center',
  },
  statNumber: {
    color: '#111',
    fontSize: 16,
    fontWeight: 'bold',
  },
  statLabel: {
    color: '#667085',
    fontSize: 14,
  },
  buttonsContainer: {
    alignItems: 'center',
  },
  button: {
    backgroundColor: '#1a73e8',
    padding: 10,
    borderRadius: 5,
    marginVertical: 5,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOpacity: 0.1,
    shadowRadius: 6,
    shadowOffset: {width: 0, height: 2},
    elevation: 3,
  },
  buttonText: {
    color: '#fff',
    fontSize: 16,
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
});
export default ProfileScreen;
