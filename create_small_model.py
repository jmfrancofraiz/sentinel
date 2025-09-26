#!/usr/bin/env python3
"""
Create a small TensorFlow Lite model for WhatsApp contact name extraction.
This model is optimized for mobile devices with limited memory.
"""

import tensorflow as tf
import numpy as np
import os

def create_small_contact_detection_model():
    """
    Create a very small neural network model for contact name extraction.
    Input: Text sequences (max 200 characters)
    Output: Contact names (max 50 characters)
    """
    
    # Model parameters - much smaller
    MAX_INPUT_LENGTH = 200
    MAX_OUTPUT_LENGTH = 50
    VOCAB_SIZE = 256  # Only ASCII characters
    
    # Input layer
    input_text = tf.keras.Input(shape=(MAX_INPUT_LENGTH,), name='input_text', dtype=tf.float32)
    
    # Very simple dense layers for TFLite compatibility
    dense1 = tf.keras.layers.Dense(64, activation='relu', name='dense1')(input_text)
    dropout1 = tf.keras.layers.Dropout(0.1, name='dropout1')(dense1)
    
    dense2 = tf.keras.layers.Dense(32, activation='relu', name='dense2')(dropout1)
    dropout2 = tf.keras.layers.Dropout(0.1, name='dropout2')(dense2)
    
    # Output layer - much smaller vocabulary
    output = tf.keras.layers.Dense(
        MAX_OUTPUT_LENGTH * VOCAB_SIZE,
        activation='softmax',
        name='output'
    )(dropout2)
    
    # Reshape output to (batch_size, MAX_OUTPUT_LENGTH, VOCAB_SIZE)
    output_reshaped = tf.keras.layers.Reshape(
        (MAX_OUTPUT_LENGTH, VOCAB_SIZE),
        name='output_reshaped'
    )(output)
    
    # Create model
    model = tf.keras.Model(inputs=input_text, outputs=output_reshaped, name='small_contact_detection_model')
    
    return model

def generate_simple_training_data():
    """
    Generate simple training data for the small model.
    """
    # Very simple training examples
    training_examples = [
        ("John Smith\nOnline\nLast seen today", "John Smith"),
        ("Maria Garcia\nTyping...\nLast seen yesterday", "Maria Garcia"),
        ("Dr. Johnson\nOnline\nLast seen 5 minutes ago", "Dr. Johnson"),
        ("Sarah Wilson\nLast seen today", "Sarah Wilson"),
        ("Mike Chen\nOnline\nLast seen 1 hour ago", "Mike Chen"),
        ("Anna Rodriguez\nLast seen yesterday", "Anna Rodriguez"),
        ("Prof. Brown\nOnline\nLast seen 30 minutes ago", "Prof. Brown"),
        ("Lisa Johnson\nTyping...\nLast seen today", "Lisa Johnson"),
        ("David Kim\nLast seen 2 hours ago", "David Kim"),
        ("Emma Davis\nOnline\nLast seen 15 minutes ago", "Emma Davis"),
    ]
    
    # Generate more examples
    names = ["Alex", "Chris", "Taylor", "Jordan", "Casey", "Morgan", "Riley", "Avery", "Quinn", "Blake"]
    surnames = ["Anderson", "Taylor", "Thomas", "Jackson", "White", "Harris", "Martin", "Thompson", "Garcia", "Martinez"]
    
    for i in range(20):  # Much fewer examples
        first_name = np.random.choice(names)
        last_name = np.random.choice(surnames)
        full_name = f"{first_name} {last_name}"
        
        status_options = ["Online", "Last seen today", "Last seen yesterday", "Typing..."]
        status = np.random.choice(status_options)
        
        text = f"{full_name}\n{status}\nLast seen today"
        training_examples.append((text, full_name))
    
    return training_examples

def preprocess_text_simple(text, max_length=200):
    """Convert text to simple character indices (ASCII only)."""
    char_indices = []
    for char in text[:max_length]:
        char_code = min(ord(char), 255)  # Cap at 255 for ASCII
        char_indices.append(char_code)
    
    # Pad to max_length
    while len(char_indices) < max_length:
        char_indices.append(0)  # Padding token
    
    return char_indices

def preprocess_contact_name_simple(name, max_length=50):
    """Convert contact name to simple character indices (ASCII only)."""
    char_indices = []
    for char in name[:max_length]:
        char_code = min(ord(char), 255)  # Cap at 255 for ASCII
        char_indices.append(char_code)
    
    # Pad to max_length
    while len(char_indices) < max_length:
        char_indices.append(0)
    
    return char_indices

def create_training_data():
    """Create training data for the small model."""
    training_examples = generate_simple_training_data()
    
    X = []
    y = []
    
    for text, contact_name in training_examples:
        # Preprocess input text
        text_indices = preprocess_text_simple(text)
        X.append(text_indices)
        
        # Preprocess output contact name
        name_indices = preprocess_contact_name_simple(contact_name)
        y.append(name_indices)
    
    return np.array(X, dtype=np.float32), np.array(y, dtype=np.float32)

def train_small_model():
    """Train the small contact detection model."""
    print("Creating small model...")
    model = create_small_contact_detection_model()
    
    print("Model architecture:")
    model.summary()
    
    print("Generating training data...")
    X_train, y_train = create_training_data()
    
    print(f"Training data shape: X={X_train.shape}, y={y_train.shape}")
    
    # Compile model
    model.compile(
        optimizer='adam',
        loss='sparse_categorical_crossentropy',
        metrics=['accuracy']
    )
    
    # Train model with fewer epochs
    print("Training small model...")
    history = model.fit(
        X_train, y_train,
        epochs=5,  # Fewer epochs
        batch_size=4,  # Smaller batch size
        validation_split=0.2,
        verbose=1
    )
    
    return model

def convert_to_tflite_small(model, output_path):
    """Convert the trained model to TensorFlow Lite format with aggressive optimization."""
    print(f"Converting small model to TensorFlow Lite format...")
    
    # Convert to TensorFlow Lite
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    
    # Aggressive optimization for size
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    converter.target_spec.supported_types = [tf.float16]  # Use float16 for smaller size
    
    # Convert
    tflite_model = converter.convert()
    
    # Save model
    with open(output_path, 'wb') as f:
        f.write(tflite_model)
    
    print(f"Small model saved to: {output_path}")
    print(f"Model size: {len(tflite_model) / 1024 / 1024:.2f} MB")

def main():
    """Main function to create and train the small model."""
    print("WhatsApp Small Contact Detection Model Creator")
    print("=" * 50)
    
    # Create output directory
    output_dir = "app/src/main/assets/models"
    os.makedirs(output_dir, exist_ok=True)
    
    # Train small model
    model = train_small_model()
    
    # Convert to TensorFlow Lite
    tflite_path = os.path.join(output_dir, "contact_detection_model.tflite")
    convert_to_tflite_small(model, tflite_path)
    
    print("\nSmall model creation completed successfully!")
    print(f"TensorFlow Lite model saved to: {tflite_path}")

if __name__ == "__main__":
    main()
