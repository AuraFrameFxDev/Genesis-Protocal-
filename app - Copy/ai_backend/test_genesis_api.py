import asyncio
import json
import pytest
# Import the module under test
from app.ai_backend.genesis_api import (
    GenesisAPIClient,
    GenesisAPIError,
    RateLimitError,
    AuthenticationError,
    ValidationError,
    APIResponse,
    ModelConfig,
    ChatMessage,
    ChatCompletion
)
from datetime import datetime, timezone
from typing import Dict, Any, List, Optional
from unittest.mock import Mock, patch, AsyncMock, MagicMock


class TestGenesisAPIClient:
    """Test suite for GenesisAPIClient class."""

    @pytest.fixture
    def mock_config(self):
        """
        Provides a sample configuration dictionary for initializing a GenesisAPIClient during testing.
        """
        return {
            'api_key': 'test-api-key-123',
            'base_url': 'https://api.genesis.ai/v1',
            'timeout': 30,
            'max_retries': 3
        }

    @pytest.fixture
    def client(self, mock_config):
        """
        Create and return a `GenesisAPIClient` instance initialized with the provided configuration dictionary.
        
        Parameters:
            mock_config (dict): Configuration parameters for initializing the client.
        
        Returns:
            GenesisAPIClient: An instance of the API client configured with the given settings.
        """
        return GenesisAPIClient(**mock_config)

    @pytest.fixture
    def sample_messages(self):
        """
        Return a list of sample ChatMessage objects simulating a basic conversation.
        
        Returns:
            List[ChatMessage]: Messages representing a system prompt, user question, and assistant reply for use in tests.
        """
        return [
            ChatMessage(role="system", content="You are a helpful assistant."),
            ChatMessage(role="user", content="What is the weather like today?"),
            ChatMessage(role="assistant", content="I don't have access to real-time weather data.")
        ]

    @pytest.fixture
    def sample_model_config(self):
        """
        Return a ModelConfig instance with preset parameters for use in tests.
        
        Returns:
            ModelConfig: A model configuration with predefined values suitable for testing.
        """
        return ModelConfig(
            name="genesis-gpt-4",
            max_tokens=1000,
            temperature=0.7,
            top_p=0.9,
            frequency_penalty=0.0,
            presence_penalty=0.0
        )

    def test_client_initialization_with_valid_config(self, mock_config):
        """
        Test that GenesisAPIClient initializes with the correct attributes when provided a valid configuration.
        
        Asserts that the client's api_key, base_url, timeout, and max_retries match the values in the configuration dictionary.
        """
        client = GenesisAPIClient(**mock_config)
        assert client.api_key == mock_config['api_key']
        assert client.base_url == mock_config['base_url']
        assert client.timeout == mock_config['timeout']
        assert client.max_retries == mock_config['max_retries']

    def test_client_initialization_with_minimal_config(self):
        """
        Test that the GenesisAPIClient initializes with only the required API key, using default values for all other parameters.
        """
        client = GenesisAPIClient(api_key='test-key')
        assert client.api_key == 'test-key'
        assert client.base_url is not None
        assert client.timeout > 0
        assert client.max_retries >= 0

    def test_client_initialization_missing_api_key(self):
        """
        Test that initializing GenesisAPIClient without an API key raises a ValueError.
        """
        with pytest.raises(ValueError, match="API key is required"):
            GenesisAPIClient()

    def test_client_initialization_invalid_timeout(self):
        """
        Test that initializing GenesisAPIClient with a non-positive timeout value raises a ValueError.
        """
        with pytest.raises(ValueError, match="Timeout must be positive"):
            GenesisAPIClient(api_key='test-key', timeout=-1)
        with pytest.raises(ValueError, match="Timeout must be positive"):
            GenesisAPIClient(api_key='test-key', timeout=0)

    def test_client_initialization_invalid_max_retries(self):
        """
        Test that initializing GenesisAPIClient with a negative max_retries raises a ValueError.
        """
        with pytest.raises(ValueError, match="Max retries must be non-negative"):
            GenesisAPIClient(api_key='test-key', max_retries=-1)

    @pytest.mark.asyncio
    async def test_chat_completion_success(self, client, sample_messages, sample_model_config):
        """
        Test that `create_chat_completion` returns a valid `ChatCompletion` object on a successful API response.
        
        Asserts that the returned object contains the expected ID, model, choices, and usage fields.
        """
        mock_response = {
            'id': 'chat-123',
            'object': 'chat.completion',
            'created': int(datetime.now(timezone.utc).timestamp()),
            'model': 'genesis-gpt-4',
            'choices': [{
                'index': 0,
                'message': {
                    'role': 'assistant',
                    'content': 'The weather looks pleasant today!'
                },
                'finish_reason': 'stop'
            }],
            'usage': {
                'prompt_tokens': 25,
                'completion_tokens': 8,
                'total_tokens': 33
            }
        }
        with patch('aiohttp.ClientSession.post') as mock_post:
            mock_post.return_value.__aenter__.return_value.json = AsyncMock(
                return_value=mock_response)
            mock_post.return_value.__aenter__.return_value.status = 200

            result = await client.create_chat_completion(
                messages=sample_messages,
                model_config=sample_model_config
            )

            assert isinstance(result, ChatCompletion)
            assert result.id == 'chat-123'
            assert result.model == 'genesis-gpt-4'
            assert len(result.choices) == 1
            assert result.choices[0].message.content == 'The weather looks pleasant today!'
            assert result.usage.total_tokens == 33

    @pytest.mark.asyncio
    async def test_chat_completion_with_streaming(self, client, sample_messages,
                                                  sample_model_config):
        """
                                                  Test that chat completion streaming yields the correct sequence of response chunks.
                                                  
                                                  Verifies that the asynchronous streaming API returns the expected sequence of content fragments and finish reason when generating a chat completion.
                                                  """
                                                  mock_chunks = [
            {'choices': [{'delta': {'content': 'The'}}]},
            {'choices': [{'delta': {'content': ' weather'}}]},
            {'choices': [{'delta': {'content': ' is nice'}}]},
            {'choices': [{'delta': {}, 'finish_reason': 'stop'}]}
        ]

        async def mock_stream():
            """
            Asynchronously yields each encoded JSON chunk from the mock_chunks list to simulate a streaming API response.
            
            Yields:
                bytes: Encoded JSON representation of each chunk in mock_chunks.
            """
            for chunk in mock_chunks:
                yield json.dumps(chunk).encode()

        with patch('aiohttp.ClientSession.post') as mock_post:
            mock_post.return_value.__aenter__.return_value.content.iter_chunked = AsyncMock(
                return_value=mock_stream())
            mock_post.return_value.__aenter__.return_value.status = 200

            chunks = []
            async for chunk in client.create_chat_completion_stream(
                    messages=sample_messages,
                    model_config=sample_model_config
            ):
                chunks.append(chunk)

            assert len(chunks) == 4
            assert chunks[0].choices[0].delta.content == 'The'
            assert chunks[-1].choices[0].finish_reason == 'stop'

    @pytest.mark.asyncio
    async def test_chat_completion_authentication_error(self, client, sample_messages,
                                                        sample_model_config):
        """
                                                        Test that an authentication error during chat completion raises an AuthenticationError with the expected message.
                                                        
                                                        Asserts that when the API responds with a 401 status and an error message, the client raises AuthenticationError containing the correct message.
                                                        """
                                                        with patch('aiohttp.ClientSession.post') as mock_post:
            mock_post.return_value.__aenter__.return_value.status = 401
            mock_post.return_value.__aenter__.return_value.json = AsyncMock(
                return_value={'error': {'message': 'Invalid API key'}}
            )
            with pytest.raises(AuthenticationError, match="Invalid API key"):
                await client.create_chat_completion(
                    messages=sample_messages,
                    model_config=sample_model_config
                )

    @pytest.mark.asyncio
    async def test_chat_completion_rate_limit_error(self, client, sample_messages,
                                                    sample_model_config):
        """
                                                    Test that a rate limit error (HTTP 429) from the API raises a RateLimitError with the correct retry interval.
                                                    
                                                    Asserts that the RateLimitError exception includes the expected retry_after value from the response headers.
                                                    """
                                                    with patch('aiohttp.ClientSession.post') as mock_post:
            mock_post.return_value.__aenter__.return_value.status = 429
            mock_post.return_value.__aenter__.return_value.json = AsyncMock(
                return_value={'error': {'message': 'Rate limit exceeded'}}
            )
            mock_post.return_value.__aenter__.return_value.headers = {'Retry-After': '60'}
            with pytest.raises(RateLimitError) as exc_info:
                await client.create_chat_completion(
                    messages=sample_messages,
                    model_config=sample_model_config
                )
            assert exc_info.value.retry_after == 60

    @pytest.mark.asyncio
    async def test_chat_completion_validation_error(self, client, sample_model_config):
        """
        Test that creating a chat completion with an invalid message role raises a ValidationError.
        
        Asserts that the client raises a ValidationError with the expected error message when the API returns a 400 status and an error indicating an invalid message role.
        """
        invalid_messages = [ChatMessage(role="invalid_role", content="This should fail")]
        with patch('aiohttp.ClientSession.post') as mock_post:
            mock_post.return_value.__aenter__.return_value.status = 400
            mock_post.return_value.__aenter__.return_value.json = AsyncMock(
                return_value={'error': {'message': 'Invalid message role'}}
            )
            with pytest.raises(ValidationError, match="Invalid message role"):
                await client.create_chat_completion(
                    messages=invalid_messages,
                    model_config=sample_model_config
                )

    @pytest.mark.asyncio
    async def test_chat_completion_server_error_with_retry(self, client, sample_messages,
                                                           sample_model_config):
        """
                                                           Test that the client retries on server errors and succeeds after multiple failures.
                                                           
                                                           Simulates two consecutive 500 Internal Server Error responses followed by a successful response, verifying that the client retries the correct number of times and returns the expected chat completion result.
                                                           """
                                                           call_count = 0

        async def mock_post_with_failure(*args, **kwargs):
            """
            Simulates an asynchronous HTTP POST request that returns a 500 status code with an error payload on the first two calls, then returns a 200 status code with a successful chat completion payload on subsequent calls.
            
            Returns:
                Mock: A mock response object with the appropriate status and JSON payload based on the number of times the function has been called.
            """
            nonlocal call_count
            call_count += 1
            mock_response = Mock()
            if call_count <= 2:
                mock_response.status = 500
                mock_response.json = AsyncMock(
                    return_value={'error': {'message': 'Internal server error'}}
                )
            else:
                mock_response.status = 200
                mock_response.json = AsyncMock(return_value={
                    'id': 'chat-retry-success',
                    'object': 'chat.completion',
                    'created': int(datetime.now(timezone.utc).timestamp()),
                    'model': 'genesis-gpt-4',
                    'choices': [
                        {'message': {'role': 'assistant', 'content': 'Success after retry'}}],
                    'usage': {'total_tokens': 10}
                })
            return mock_response

        with patch('aiohttp.ClientSession.post', side_effect=mock_post_with_failure):
            with patch('asyncio.sleep'):
                result = await client.create_chat_completion(
                    messages=sample_messages,
                    model_config=sample_model_config
                )
                assert result.id == 'chat-retry-success'
                assert call_count == 3

    @pytest.mark.asyncio
    async def test_chat_completion_max_retries_exceeded(self, client, sample_messages,
                                                        sample_model_config):
        """
                                                        Test that `create_chat_completion` raises `GenesisAPIError` when the maximum number of retries is exceeded due to repeated server errors.
                                                        """
                                                        with patch('aiohttp.ClientSession.post') as mock_post:
            mock_post.return_value.__aenter__.return_value.status = 500
            mock_post.return_value.__aenter__.return_value.json = AsyncMock(
                return_value={'error': {'message': 'Internal server error'}}
            )
            with patch('asyncio.sleep'):
                with pytest.raises(GenesisAPIError, match="Internal server error"):
                    await client.create_chat_completion(
                        messages=sample_messages,
                        model_config=sample_model_config
                    )

    @pytest.mark.asyncio
    async def test_chat_completion_network_timeout(self, client, sample_messages,
                                                   sample_model_config):
        """
                                                   Test that a network timeout during chat completion raises a GenesisAPIError with a timeout message.
                                                   
                                                   Asserts that when a timeout occurs during the chat completion API call, the client raises a GenesisAPIError containing the phrase "Request timeout".
                                                   """
                                                   with patch('aiohttp.ClientSession.post', side_effect=asyncio.TimeoutError()):
            with pytest.raises(GenesisAPIError, match="Request timeout"):
                await client.create_chat_completion(
                    messages=sample_messages,
                    model_config=sample_model_config
                )

    @pytest.mark.asyncio
    async def test_chat_completion_connection_error(self, client, sample_messages,
                                                    sample_model_config):
        """
                                                    Test that a connection error during chat completion raises a GenesisAPIError with an appropriate message.
                                                    """
                                                    import aiohttp
        with patch('aiohttp.ClientSession.post', side_effect=aiohttp.ClientConnectionError()):
            with pytest.raises(GenesisAPIError, match="Connection error"):
                await client.create_chat_completion(
                    messages=sample_messages,
                    model_config=sample_model_config
                )

    def test_validate_messages_empty_list(self, client):
        """
        Test that validating an empty message list raises a ValidationError.
        """
        with pytest.raises(ValidationError, match="Messages cannot be empty"):
            client._validate_messages([])

    def test_validate_messages_invalid_role(self, client):
        """
        Test that _validate_messages raises ValidationError for messages with an invalid role.
        """
        invalid_messages = [ChatMessage(role="invalid", content="Test content")]
        with pytest.raises(ValidationError, match="Invalid message role"):
            client._validate_messages(invalid_messages)

    def test_validate_messages_empty_content(self, client):
        """
        Test that validating messages with empty content raises a ValidationError.
        
        Asserts that the client raises a ValidationError with the appropriate message when a ChatMessage has an empty content field.
        """
        invalid_messages = [ChatMessage(role="user", content="")]
        with pytest.raises(ValidationError, match="Message content cannot be empty"):
            client._validate_messages(invalid_messages)

    def test_validate_messages_content_too_long(self, client):
        """
        Test that validating a chat message with excessively long content raises a ValidationError.
        
        Ensures that the client enforces content length limits on chat messages and raises the appropriate exception when exceeded.
        """
        long_content = "x" * 100000
        invalid_messages = [ChatMessage(role="user", content=long_content)]
        with pytest.raises(ValidationError, match="Message content too long"):
            client._validate_messages(invalid_messages)

    def test_validate_model_config_invalid_temperature(self, client, sample_model_config):
        """
        Test that a ValidationError is raised for model configurations with temperature values outside the valid range.
        
        Verifies that the client's model configuration validation rejects temperatures less than 0 or greater than 2.
        """
        sample_model_config.temperature = -0.5
        with pytest.raises(ValidationError, match="Temperature must be between 0 and 2"):
            client._validate_model_config(sample_model_config)
        sample_model_config.temperature = 2.5
        with pytest.raises(ValidationError, match="Temperature must be between 0 and 2"):
            client._validate_model_config(sample_model_config)

    def test_validate_model_config_invalid_max_tokens(self, client, sample_model_config):
        """
        Test that _validate_model_config raises ValidationError when max_tokens is zero or negative.
        
        Ensures that the client enforces a positive value for max_tokens in the model configuration by raising a ValidationError for invalid values.
        """
        sample_model_config.max_tokens = 0
        with pytest.raises(ValidationError, match="Max tokens must be positive"):
            client._validate_model_config(sample_model_config)
        sample_model_config.max_tokens = -100
        with pytest.raises(ValidationError, match="Max tokens must be positive"):
            client._validate_model_config(sample_model_config)

    def test_validate_model_config_invalid_top_p(self, client, sample_model_config):
        """
        Test that _validate_model_config raises ValidationError for top_p values outside the range [0, 1].
        
        Verifies that providing a top_p value less than 0 or greater than 1 in the model configuration triggers a ValidationError with the appropriate message.
        """
        sample_model_config.top_p = -0.1
        with pytest.raises(ValidationError, match="Top_p must be between 0 and 1"):
            client._validate_model_config(sample_model_config)
        sample_model_config.top_p = 1.5
        with pytest.raises(ValidationError, match="Top_p must be between 0 and 1"):
            client._validate_model_config(sample_model_config)

    @pytest.mark.asyncio
    async def test_list_models_success(self, client):
        """
        Test that `list_models` returns a list of model objects on a successful API response.
        
        Asserts that the returned list contains the expected number of models with correct IDs.
        """
        mock_response = {
            'object': 'list',
            'data': [
                {'id': 'genesis-gpt-4', 'object': 'model', 'created': 1677610602},
                {'id': 'genesis-gpt-3.5-turbo', 'object': 'model', 'created': 1677610602}
            ]
        }
        with patch('aiohttp.ClientSession.get') as mock_get:
            mock_get.return_value.__aenter__.return_value.json = AsyncMock(
                return_value=mock_response)
            mock_get.return_value.__aenter__.return_value.status = 200
            models = await client.list_models()
            assert len(models) == 2
            assert models[0].id == 'genesis-gpt-4'
            assert models[1].id == 'genesis-gpt-3.5-turbo'

    @pytest.mark.asyncio
    async def test_get_model_success(self, client):
        """
        Test that `get_model` retrieves a model by ID and returns an object with correct attributes.
        
        Asserts that the returned model's `id` and `owned_by` fields match the expected values from the mocked API response.
        """
        mock_response = {
            'id': 'genesis-gpt-4',
            'object': 'model',
            'created': 1677610602,
            'owned_by': 'genesis-ai',
            'permission': []
        }
        with patch('aiohttp.ClientSession.get') as mock_get:
            mock_get.return_value.__aenter__.return_value.json = AsyncMock(
                return_value=mock_response)
            mock_get.return_value.__aenter__.return_value.status = 200
            model = await client.get_model('genesis-gpt-4')
            assert model.id == 'genesis-gpt-4'
            assert model.owned_by == 'genesis-ai'

    @pytest.mark.asyncio
    async def test_get_model_not_found(self, client):
        """
        Test that requesting a nonexistent model raises a GenesisAPIError with the correct error message.
        """
        with patch('aiohttp.ClientSession.get') as mock_get:
            mock_get.return_value.__aenter__.return_value.status = 404
            mock_get.return_value.__aenter__.return_value.json = AsyncMock(
                return_value={'error': {'message': 'Model not found'}}
            )
            with pytest.raises(GenesisAPIError, match="Model not found"):
                await client.get_model('non-existent-model')

    def test_build_headers(self, client):
        """
        Test that the client's _build_headers method includes the correct authorization, content type, and user agent headers.
        """
        headers = client._build_headers()
        assert 'Authorization' in headers
        assert headers['Authorization'] == f'Bearer {client.api_key}'
        assert headers['Content-Type'] == 'application/json'
        assert 'User-Agent' in headers

    def test_build_headers_with_custom_headers(self, client):
        """
        Test that the client's header construction method merges custom headers with default headers.
        
        Asserts that custom headers are present in the result and that required default headers are included.
        """
        custom_headers = {'X-Custom-Header': 'custom-value'}
        headers = client._build_headers(custom_headers)
        assert headers['X-Custom-Header'] == 'custom-value'
        assert 'Authorization' in headers
        assert headers['Content-Type'] == 'application/json'

    @pytest.mark.asyncio
    async def test_context_manager_usage(self, mock_config):
        """
        Test that GenesisAPIClient correctly opens and closes its session when used as an asynchronous context manager.
        
        Asserts that the session is active within the context and properly closed after exiting the context.
        """
        async with GenesisAPIClient(**mock_config) as client:
            assert client.session is not None
        assert client.session.closed

    @pytest.mark.asyncio
    async def test_close_client_explicitly(self, client):
        """
        Test that explicitly calling the client's close method properly closes the session.
        """
        await client.close()
        assert client.session.closed

    @pytest.mark.parametrize("status_code,expected_exception", [
        (400, ValidationError),
        (401, AuthenticationError),
        (403, AuthenticationError),
        (429, RateLimitError),
        (500, GenesisAPIError),
        (502, GenesisAPIError),
        (503, GenesisAPIError),
    ])
    @pytest.mark.asyncio
    async def test_error_handling_by_status_code(self, client, status_code, expected_exception):
        """
        Verifies that the client raises the appropriate exception for a given HTTP status code during chat completion creation.
        
        Parameters:
            status_code (int): The simulated HTTP status code.
            expected_exception (Exception): The exception type expected to be raised for the simulated status code.
        """
        with patch('aiohttp.ClientSession.post') as mock_post:
            mock_post.return_value.__aenter__.return_value.status = status_code
            mock_post.return_value.__aenter__.return_value.json = AsyncMock(
                return_value={'error': {'message': f'Error {status_code}'}}
            )
            with pytest.raises(expected_exception):
                await client.create_chat_completion(
                    messages=[ChatMessage(role="user", content="test")],
                    model_config=ModelConfig(name="test-model")
                )


class TestDataModels:
    """Test suite for data model classes."""

    def test_chat_message_creation(self):
        """
        Test creation of a ChatMessage instance with specified role and content, ensuring the default name is None.
        """
        message = ChatMessage(role="user", content="Hello, world!")
        assert message.role == "user"
        assert message.content == "Hello, world!"
        assert message.name is None

    def test_chat_message_with_name(self):
        message = ChatMessage(role="user", content="Hello", name="John")
        assert message.name == "John"

    def test_model_config_creation(self):
        """
        Test that a ModelConfig instance is created with the correct attribute values.
        """
        config = ModelConfig(
            name="genesis-gpt-4",
            max_tokens=1000,
            temperature=0.7
        )
        assert config.name == "genesis-gpt-4"
        assert config.max_tokens == 1000
        assert config.temperature == 0.7

    def test_model_config_defaults(self):
        """
        Verify that ModelConfig assigns default values to max_tokens, temperature, and top_p when these parameters are not specified.
        """
        config = ModelConfig(name="test-model")
        assert config.name == "test-model"
        assert config.max_tokens is not None
        assert config.temperature is not None
        assert config.top_p is not None

    def test_api_response_creation(self):
        """
        Test creation of an APIResponse object with specified status code, data, and headers.
        
        Asserts that the APIResponse instance correctly stores the provided values.
        """
        response = APIResponse(
            status_code=200,
            data={'message': 'success'},
            headers={'Content-Type': 'application/json'}
        )
        assert response.status_code == 200
        assert response.data['message'] == 'success'
        assert response.headers['Content-Type'] == 'application/json'

    def test_chat_completion_creation(self):
        """
        Test that a ChatCompletion object is instantiated with the expected attribute values.
        """
        completion = ChatCompletion(
            id="chat-123",
            object="chat.completion",
            created=1677610602,
            model="genesis-gpt-4",
            choices=[],
            usage={'total_tokens': 100}
        )
        assert completion.id == "chat-123"
        assert completion.model == "genesis-gpt-4"
        assert completion.usage['total_tokens'] == 100


class TestExceptionClasses:
    """Test suite for custom exception classes."""

    def test_genesis_api_error(self):
        """
        Test that the GenesisAPIError exception correctly sets its message and status code.
        """
        error = GenesisAPIError("Test error message", status_code=500)
        assert str(error) == "Test error message"
        assert error.status_code == 500

    def test_authentication_error(self):
        """
        Test that AuthenticationError correctly sets its message and inherits from GenesisAPIError.
        """
        error = AuthenticationError("Invalid API key")
        assert str(error) == "Invalid API key"
        assert isinstance(error, GenesisAPIError)

    def test_rate_limit_error(self):
        """
        Test that RateLimitError sets the message and retry_after attributes and inherits from GenesisAPIError.
        """
        error = RateLimitError("Rate limit exceeded", retry_after=60)
        assert str(error) == "Rate limit exceeded"
        assert error.retry_after == 60
        assert isinstance(error, GenesisAPIError)

    def test_validation_error(self):
        """
        Test that ValidationError correctly stores and returns its message and inherits from GenesisAPIError.
        """
        error = ValidationError("Invalid input data")
        assert str(error) == "Invalid input data"
        assert isinstance(error, GenesisAPIError)


class TestUtilityFunctions:
    """Test suite for utility functions in the genesis_api module."""

    def test_format_timestamp(self):
        """
        Test that the `format_timestamp` utility function returns a non-empty string when given a numeric timestamp.
        """
        from app.ai_backend.genesis_api import format_timestamp
        timestamp = 1677610602
        formatted = format_timestamp(timestamp)
        assert isinstance(formatted, str)
        assert len(formatted) > 0

    def test_calculate_token_usage(self):
        """
        Test that `calculate_token_usage` returns a dictionary with an estimated token count for a list of chat messages.
        
        Verifies that the returned dictionary contains the 'estimated_tokens' key and is of the correct type.
        """
        from app.ai_backend.genesis_api import calculate_token_usage
        messages = [
            ChatMessage(role="user", content="Hello"),
            ChatMessage(role="assistant", content="Hi there!")
        ]
        usage = calculate_token_usage(messages)
        assert isinstance(usage, dict)
        assert 'estimated_tokens' in usage

    @pytest.mark.parametrize("content,expected_tokens", [
        ("Hello", 1),
        ("Hello world", 2),
        ("", 0),
        ("A very long message with many words", 7),
    ])
    def test_estimate_tokens(self, content, expected_tokens):
        """
        Verify that the `estimate_tokens` utility function returns the correct token count for a given input string.
        
        Parameters:
            content (str): The input string to estimate tokens for.
            expected_tokens (int): The expected token count for the input string.
        """
        from app.ai_backend.genesis_api import estimate_tokens
        tokens = estimate_tokens(content)
        assert tokens == expected_tokens
